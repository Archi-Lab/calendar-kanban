package canban.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.util.Preconditions;
import com.vaadin.flow.component.Component;

import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SimpleAuthorizationCodeInstalledApp{

    private final AuthorizationCodeFlow flow;
    private final VerificationCodeReceiver receiver;
    private static final Logger LOGGER = Logger.getLogger(SimpleAuthorizationCodeInstalledApp.class.getName());
    private static Component rootComponent;

    public SimpleAuthorizationCodeInstalledApp(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver) {
        super();
        this.flow = (AuthorizationCodeFlow)Preconditions.checkNotNull(flow);
        this.receiver = (VerificationCodeReceiver)Preconditions.checkNotNull(receiver);
    }

    public Credential authorize(String userId) throws IOException {
        Credential var7;
        try {
            Credential credential = this.flow.loadCredential(userId);
            if (credential != null && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() == null || credential.getExpiresInSeconds() > 60L)) {
                return credential;
            }

            String redirectUri = this.receiver.getRedirectUri();
            AuthorizationCodeRequestUrl authorizationUrl = this.flow.newAuthorizationUrl().setRedirectUri(redirectUri);
            this.onAuthorization(authorizationUrl);
            String code = this.receiver.waitForCode();
            TokenResponse response = this.flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            var7 = this.flow.createAndStoreCredential(response, userId);
        } finally {
            this.receiver.stop();
        }

        return var7;
    }

    protected void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl) throws IOException {
        String url= authorizationUrl.build();

        Preconditions.checkNotNull(url);
        browse(url);
    }

    public static void browse(String url) {
        System.out.println("");
        PrintStream var10000 = System.out;
        String var10002 = String.valueOf(url);
        String var10001;
        if (var10002.length() != 0) {
            var10001 = "  ".concat(var10002);
        } else {
            String var10003 = new String();
            var10001 = var10003;
            var10003=("  ");
        }

        var10000.println(var10001);

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    System.out.println("Attempting to open that address in the default browser now...");
                    desktop.browse(URI.create(url));
                }
            }else{
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (IOException | InternalError var2) {
            LOGGER.log(Level.WARNING, "Unable to open browser", var2);
        }

    }


    public final AuthorizationCodeFlow getFlow() {
        return this.flow;
    }

    public final VerificationCodeReceiver getReceiver() {
        return this.receiver;
    }
}
