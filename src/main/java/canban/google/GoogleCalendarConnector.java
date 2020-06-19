package canban.google;
import authentication.CurrentUser;
import canban.repository.UserRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.vaadin.flow.server.VaadinService.getCurrentRequest;

public class GoogleCalendarConnector {
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";



    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleCalendarConnector.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));


        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new SimpleAuthorizationCodeInstalledApp(flow, receiver).authorize(CurrentUser.getRole().getName()+CurrentUser.getRole().getId().toString());
    }

    public static List<Event> connect(UserRepository userRepository) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        List<Event> items = null;
        LocalTime events_Timestamp = null;
        LocalDate events_Datestamp = null;

        items = (List<Event>) getCurrentRequest().getWrappedSession().getAttribute(
                "CURRENT_USER_SESSION_GOOGLE_EVENTS");
        events_Timestamp = (LocalTime) getCurrentRequest().getWrappedSession().getAttribute(
                "CURRENT_USER_SESSION_GOOGLE_EVENTS_TIMESTAMP");
        events_Datestamp = (LocalDate) getCurrentRequest().getWrappedSession().getAttribute(
                "CURRENT_USER_SESSION_GOOGLE_EVENTS_TIMESTAMP_DATE");
        if(items==null || events_Timestamp==null || events_Datestamp==null ||LocalTime.now().minusMinutes(2).isAfter(events_Timestamp)||LocalDate.now().isAfter(events_Datestamp)){

            CurrentUser.getRole().setConnectGoogle(false);
            userRepository.save(CurrentUser.getRole());
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            CurrentUser.getRole().setConnectGoogle(true);
            userRepository.save(CurrentUser.getRole());

            DateTime now = new DateTime(System.currentTimeMillis());

            int n = CurrentUser.getRole().getNweeksValue();
            LocalDate nowN = LocalDate.now().plusWeeks(2);
            TemporalField fieldISON = WeekFields.of(Locale.GERMANY).dayOfWeek();
            //Plus 1 Tag, weil der Zeitstempel 00:00 Uhr setzt
            LocalDate endDateN = nowN.plusWeeks(n-1).with(fieldISON,7).plusDays(1);
            Date date = Date.from(endDateN.atStartOfDay(ZoneId.systemDefault()).toInstant());
            DateTime end = new DateTime(date);
            Events events = service.events().list("primary")
                    .setTimeMin(now)
                    .setTimeMax(end)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            items = events.getItems();
            if (items.isEmpty()) {
                System.out.println("No upcoming events found.");
            } else {
                System.out.println("Upcoming events");
                for (Event event : items) {
                    DateTime start = event.getStart().getDateTime();
                    if (start == null) {
                        start = event.getStart().getDate();
                    }
                    System.out.printf("%s (%s)\n", event.getSummary(), start);
                }
            }
            getCurrentRequest().getWrappedSession().setAttribute(
                    "CURRENT_USER_SESSION_GOOGLE_EVENTS",items);
            getCurrentRequest().getWrappedSession().setAttribute(
                    "CURRENT_USER_SESSION_GOOGLE_EVENTS_TIMESTAMP",LocalTime.now());
            getCurrentRequest().getWrappedSession().setAttribute(
                    "CURRENT_USER_SESSION_GOOGLE_EVENTS_TIMESTAMP_DATE",LocalDate.now());
        }
        return items;
    }
}