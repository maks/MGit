package me.sheimi.sgit;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.URIish;

import me.sheimi.android.utils.SecurePrefsHelper;
import timber.log.Timber;

/**
 *
 * ref: http://stackoverflow.com/a/15290861/85472
 */

public class AndroidJschCredentialsProvider extends org.eclipse.jgit.transport.CredentialsProvider {

    private final SecurePrefsHelper mSecPrefsHelper;

    public AndroidJschCredentialsProvider(SecurePrefsHelper securePrefsHelper) {
        mSecPrefsHelper = securePrefsHelper;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public boolean supports(CredentialItem... items) {
        return true;
    }

    @Override
    public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
        Timber.w("get for uri %s", uri);
        /*
         * The caller will have passed some number of CredentialItems. We only
         * support those of StringType (representing passphrases for private
         * keys we have on hand). We will fill in as many of those as we have
         * passphrases for (stored as encrypted SecurePreferences). Return true
         * if we were able to fill in any of them. (The API description of the
         * superclass avoids saying "any" or "all" for the return-true case; it
         * says to return true "if the request was successful and values were
         * supplied". It says to return false "if the user canceled the request
         * and did not supply all requested values". Because this is not an
         * interactive CredentialsProvider, "the user canceled the request"
         * can't ever be true, so it seems fair to reserve the false return for
         * only when we can supply none of the requested values, and return true
         * whenever we have been able to supply at least one of them, even if
         * not all.)
         */
        boolean foundAny = false;
        for (final CredentialItem item : items) {
            if (item instanceof CredentialItem.StringType) {
                Timber.w("need credential for: %s ", item.getPromptText());
                // the getPromptText() will be "Passphrase for /.../files/ssh/key_file_name_rsa"
                String prompt = item.getPromptText();
                String keyfileName = prompt.substring(prompt.lastIndexOf("/")+1, prompt.length());
                String password = mSecPrefsHelper.get(keyfileName);
                if (password != null) {
                    ((CredentialItem.StringType) item).setValue(password);
                    foundAny = true;
                }
            }
        }
        return foundAny;
    }
}
