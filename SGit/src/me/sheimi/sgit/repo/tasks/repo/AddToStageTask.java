package me.sheimi.sgit.repo.tasks.repo;

import me.sheimi.sgit.R;
import me.sheimi.sgit.database.models.Repo;

public class AddToStageTask extends RepoOpTask {

    public String mFilePattern;

    public AddToStageTask(Repo repo, String filepattern) {
        super(repo);
        mFilePattern = filepattern;
        setSuccessMsg(R.string.success_add_to_stage);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        boolean result = addToStage();
        if (!result) {
            return false;
        }
        return true;
    }

    protected void onPostExecute(Boolean isSuccess) {
        super.onPostExecute(isSuccess);
    }

    public boolean addToStage() {
        try {
            mRepo.getGit().add().addFilepattern(mFilePattern).call();
        } catch (Throwable e) {
            setException(e);
            return false;
        }
        return true;
    }
}
