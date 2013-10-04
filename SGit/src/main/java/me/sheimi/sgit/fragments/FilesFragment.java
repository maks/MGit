package me.sheimi.sgit.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.RepoDetailActivity;
import me.sheimi.sgit.activities.ViewFileActivity;
import me.sheimi.sgit.adapters.FilesListAdapter;
import me.sheimi.sgit.dialogs.ChooseCommitDialog;
import me.sheimi.sgit.listeners.OnBackClickListener;
import me.sheimi.sgit.utils.ActivityUtils;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;
import me.sheimi.sgit.utils.ViewUtils;

/**
 * Created by sheimi on 8/5/13.
 */
public class FilesFragment extends BaseFragment {

    private static String LOCAL_REPO = "local_repo";
    private static String CURRENT_DIR = "current_dir";

    private FsUtils mFsUtils;
    private RepoUtils mRepoUtils;
    private ViewUtils mViewUtils;

    private String mLocalRepo;

    private Button mCommitNameButton;
    private ImageView mCommitType;
    private ListView mFilesList;
    private FilesListAdapter mFilesListAdapter;

    private File mCurrentDir;
    private File mRootDir;

    private Git mGit;

    private RepoDetailActivity mActivity;


    public static FilesFragment newInstance(String  mLocalRepo) {
        FilesFragment fragment = new FilesFragment();
        Bundle bundle = new Bundle();
        bundle.putString(LOCAL_REPO, mLocalRepo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files, container, false);
        mActivity = (RepoDetailActivity) getActivity();
        mRepoUtils = RepoUtils.getInstance(mActivity);
        mViewUtils = ViewUtils.getInstance(mActivity);
        mFsUtils = FsUtils.getInstance(mActivity);
        mActivity.setFilesFragment(this);

        Bundle bundle = getArguments();
        String localRepoStr = bundle.getString(LOCAL_REPO);
        if (localRepoStr != null) {
            mLocalRepo = localRepoStr;
        }
        if (savedInstanceState != null) {
            String saveRepoStr = savedInstanceState.getString(LOCAL_REPO);
            if (saveRepoStr != null) {
                mLocalRepo = saveRepoStr;
            }
        }
        if (mLocalRepo == null) {
            // this will not execute, if the app runs right
            return v;
        }
        mRootDir = mFsUtils.getRepo(mLocalRepo);
        mGit = mRepoUtils.getGit(mLocalRepo);

        mCommitNameButton = (Button) v.findViewById(R.id.commitName);
        mCommitType = (ImageView) v.findViewById(R.id.commitType);
        mFilesList = (ListView) v.findViewById(R.id.filesList);

        mFilesListAdapter = new FilesListAdapter(getActivity(), new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                if (name.equals(".git")) return false;
                return true;
            }
        });
        mFilesList.setAdapter(mFilesListAdapter);


        mFilesList.setOnItemClickListener(new AdapterView.OnItemClickListener
                () {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                File file = mFilesListAdapter.getItem(position);
                if (file.isDirectory()) {
                    setCurrentDir(file);
                    return;
                }
                String mime = mFsUtils.getMimeType(file);
                if (mime.startsWith("text")) {
                    Intent intent = new Intent(getActivity(),
                            ViewFileActivity.class);
                    intent.putExtra(ViewFileActivity.TAG_FILE_NAME,
                            file.getAbsolutePath());
                    ActivityUtils.startActivity(mActivity, intent);
                    return;
                }
                mFsUtils.openFile(file);
            }
        });

        mFilesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
                                           long id) {
                final File file = mFilesListAdapter.getItem(position);
                mViewUtils.showMessageDialog(R.string.dialog_file_delete,
                        R.string.dialog_file_delete_msg, R.string.label_delete,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mFsUtils.deleteFile(file);
                                reset();
                            }
                        });
                return true;
            }
        });

        mCommitNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseCommitDialog cbd = new ChooseCommitDialog(mGit);
                cbd.show(getFragmentManager(), "choose-branch-dialog");
            }
        });

        String branchName = mRepoUtils.getBranchName(mGit);
        reset(branchName);
        if (savedInstanceState != null) {
            String currentDirPath = savedInstanceState.getString(CURRENT_DIR);
            if (currentDirPath != null) {
                mCurrentDir = new File(currentDirPath);
                setCurrentDir(mCurrentDir);
            }
        }
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOCAL_REPO, mLocalRepo);
        outState.putString(CURRENT_DIR, mCurrentDir.getAbsolutePath());
    }

    public void setCurrentDir(File dir) {
        mCurrentDir = dir;
        if (mFilesListAdapter != null) {
            mFilesListAdapter.setDir(mCurrentDir);
        }
    }

    public void resetCurrentDir() {
        setCurrentDir(mRootDir);
    }

    public void reset(String commitName) {
        int commitType = mRepoUtils.getCommitType(commitName);
        switch (commitType) {
            case RepoUtils.COMMIT_TYPE_REMOTE:
                // change the display name to local branch
                commitName = mRepoUtils.convertRemoteName(commitName);
            case RepoUtils.COMMIT_TYPE_HEAD:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_branch_w);
                break;
            case RepoUtils.COMMIT_TYPE_TAG:
                mCommitType.setVisibility(View.VISIBLE);
                mCommitType.setImageResource(R.drawable.ic_tag_w);
                break;
            case RepoUtils.COMMIT_TYPE_TEMP:
                mCommitType.setVisibility(View.GONE);
                break;
        }
        String displayName = mRepoUtils.getCommitDisplayName(commitName);
        mCommitNameButton.setText(displayName);
        resetCurrentDir();
    }

    public void reset() {
        resetCurrentDir();
    }

    public boolean newDir(String name) {
        File file = new File(mCurrentDir, name);
        if (file.exists()) {
            mViewUtils.showToastMessage(R.string.alert_file_exists);
            return false;
        }
        return file.mkdir();
    }

    public boolean newFile(String name) {
        File file = new File(mCurrentDir, name);
        Log.d("name", name);
        if (file.exists()) {
            mViewUtils.showToastMessage(R.string.alert_file_exists);
            return false;
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            mViewUtils.showToastMessage(e.getMessage());
            return false;
        }
    }


    @Override
    public OnBackClickListener getOnBackClickListener() {
        return new OnBackClickListener() {
            @Override
            public boolean onClick() {
                if (mRootDir == null || mCurrentDir == null)
                    return false;
                if (mRootDir.equals(mCurrentDir))
                    return false;
                File parent = mCurrentDir.getParentFile();
                setCurrentDir(parent);
                return true;
            }
        };
    }
}
