package me.sheimi.sgit.dialogs;

import java.io.File;
import java.util.List;

import me.sheimi.android.utils.FsUtils;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.explorer.PrivateKeyManageActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import me.sheimi.sgit.activities.BranchChooserActivity;
import android.widget.Toast;
import me.sheimi.sgit.exception.StopTaskException;
import org.eclipse.jgit.api.errors.GitAPIException;
import me.sheimi.sgit.database.models.Repo;
import org.eclipse.jgit.lib.Ref;
import android.app.DialogFragment;

/**
 * Created by sheimi on 8/24/13.
 */

public class RenameBranchDialog extends DialogFragment implements
        View.OnClickListener, DialogInterface.OnClickListener {

    private String mFromCommit;
    private EditText mNewBranchname;
    private BranchChooserActivity mActivity;
    private Repo mRepo;
    public static final String FROM_COMMIT = "from path";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        mActivity = (BranchChooserActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        Bundle args = getArguments();
        if (args != null && args.containsKey(FROM_COMMIT)) {
            mFromCommit = args.getString(FROM_COMMIT);
        }
        if (args != null && args.containsKey(Repo.TAG)) {
	    mRepo = (Repo) args.getSerializable(Repo.TAG);
	}

        builder.setTitle(getString(R.string.dialog_rename_branch_title));
        View view = mActivity.getLayoutInflater().inflate(
                R.layout.dialog_rename_branch, null);

        builder.setView(view);
        mNewBranchname = (EditText) view.findViewById(R.id.newBranchname);
        mNewBranchname.setText(Repo.getCommitDisplayName(mFromCommit));

        // set button listener
        builder.setNegativeButton(R.string.label_cancel,
                new DummyDialogListener());
        builder.setPositiveButton(R.string.label_rename,
                new DummyDialogListener());

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FROM_COMMIT, mFromCommit);
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null)
            return;
        Button positiveButton = (Button) dialog
                .getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String newBranchname = mNewBranchname.getText().toString().trim();
        if (newBranchname.equals("")) {
	    Toast.makeText(mActivity, getString(R.string.alert_new_branchname_required),
			   Toast.LENGTH_LONG).show();
            mNewBranchname
                    .setError(getString(R.string.alert_new_branchname_required));
            return;
        }


	int commitType = Repo.getCommitType(mFromCommit);
	boolean fail = false;
	try {
	    switch (commitType) {
	    case Repo.COMMIT_TYPE_HEAD:
		mRepo.getGit().branchRename()
		    .setOldName(mFromCommit)
		    .setNewName(newBranchname)
		    .call();
		break;
	    case Repo.COMMIT_TYPE_TAG:
		RevTag tag = null;
		List<Ref> refs = mRepo.getGit().tagList().call();
		for (int i = 0; i < refs.size(); ++i) {
		    if (refs.get(i).getName().equals(mFromCommit)) {
			tag = new RevWalk(mRepo.getGit().getRepository()).lookupTag(refs.get(i).getObjectId());
			break;
		    }
		}   

		if (tag == null) {
		    fail = true;
		    break;
		}

		mRepo.getGit().tag()
		    .setMessage(tag.getFullMessage())
		    .setName(newBranchname)
		    .setObjectId(tag.getObject())
		    .setTagger(tag.getTaggerIdent())
		    .call();
		mRepo.getGit().tagDelete()
		    .setTags(mFromCommit)
		    .call();
		break;
				}
	} catch (StopTaskException e) {
	    fail = true;
	} catch (GitAPIException e) {
	    fail = true;
	}
	if (fail) {
	    mActivity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			Toast.makeText(mActivity, "can't rename " + mFromCommit,
				       Toast.LENGTH_LONG).show();
		    }
		});
	}

	mActivity.refreshList();
        dismiss();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
    }

}
