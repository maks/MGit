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
        //FIXME: we will only handle the first *successfully* matched item
        for (final CredentialItem item : items) {
            if (item instanceof CredentialItem.StringType) {
                Timber.w("need credential for: %s ", item.getPromptText());
                // the getPromptText() will be "Passphrase for /.../files/ssh/key_file_name_rsa"
                String prompt = item.getPromptText();
                String keyfileName = prompt.substring(prompt.lastIndexOf("/")+1, prompt.length());
                String password = mSecPrefsHelper.get(keyfileName);
                if (password != null) {
                    ((CredentialItem.StringType) item).setValue(password);
                    return true;
                }
            }
        }
        return false;
    }
}
