package me.sheimi.sgit.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.jgit.api.Git;

import java.io.File;

import me.sheimi.sgit.R;
import me.sheimi.sgit.adapters.FilesListAdapter;
import me.sheimi.sgit.utils.FsUtils;
import me.sheimi.sgit.utils.RepoUtils;

/**
 * Created by sheimi on 8/5/13.
 */
public class FilesFragment extends BaseFragment {

    private RepoUtils mRepoUtils;
    private FsUtils mFsUtils;

    private String mCommitName;
    private String mLocalRepo;

    private TextView mCommitNameTV;
    private ListView mFilesList;
    private FilesListAdapter mFilesListAdapter;

    private File mCurrentDir;
    private File mRootDir;

    private Git mGit;

    public FilesFragment(Git git, String localRepo) {
        mGit = git;
        mLocalRepo = localRepo;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files, container, false);
        mFsUtils = FsUtils.getInstance(getActivity());
        mRepoUtils = RepoUtils.getInstance(getActivity());

        mRootDir = mFsUtils.getRepo(mLocalRepo);

        mCommitNameTV = (TextView) v.findViewById(R.id.commitName);
        mFilesList = (ListView) v.findViewById(R.id.filesList);

        setCommit(mLocalRepo);
        mFilesListAdapter = new FilesListAdapter(getActivity());
        resetCurrentDir();
        mFilesList.setAdapter(mFilesListAdapter);

        mFilesList.setOnItemClickListener(new AdapterView.OnItemClickListener
                () {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view,
                                    int position, long id) {
                File file = mFilesListAdapter.getItem(position);
                if (!file.isDirectory())
                    return;
                setCurrentDir(file);
            }
        });

        return v;
    }

    public void setCommit(String commitName) {
        mRepoUtils.checkout(mGit, commitName);
        mCommitName = commitName;
        mCommitNameTV.setText(mCommitName);
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

}
