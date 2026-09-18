package me.sheimi.sgit.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.sheimi.android.activities.SheimiFragmentActivity;
import me.sheimi.android.activities.SheimiFragmentActivity.OnBackClickListener;
import me.sheimi.android.utils.FsUtils;
import me.sheimi.sgit.R;
import me.sheimi.sgit.activities.ViewFileActivity;
import me.sheimi.sgit.adapters.FilesListAdapter;
import me.sheimi.sgit.database.models.Repo;
import me.sheimi.sgit.dialogs.RepoFileOperationDialog;
import timber.log.Timber;

/**
 * Created by sheimi on 8/5/13.
 */
public class FilesFragment extends RepoDetailFragment {

    private static String CURRENT_DIR = "current_dir";

    private ListView mFilesList;
    private FilesListAdapter mFilesListAdapter;

    private File mCurrentDir;
    private File mRootDir;

    private Repo mRepo;

    private HorizontalScrollView mBreadcrumbScroll;
    private LinearLayout mBreadcrumbContainer;

    public static FilesFragment newInstance(Repo mRepo) {
        FilesFragment fragment = new FilesFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Repo.TAG, mRepo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_files, container, false);
        getRawActivity().setFilesFragment(this);

        Bundle bundle = getArguments();
        mRepo = (Repo) bundle.getSerializable(Repo.TAG);
        if (mRepo == null && savedInstanceState != null) {
            mRepo = (Repo) savedInstanceState.getSerializable(Repo.TAG);
        }
        if (mRepo == null) {
            return v;
        }
        mRootDir = mRepo.getDir();

        mFilesList = (ListView) v.findViewById(R.id.filesList);
        mBreadcrumbScroll = (HorizontalScrollView) v.findViewById(R.id.breadcrumbScroll);
        mBreadcrumbContainer = (LinearLayout) v.findViewById(R.id.breadcrumbContainer);

        mFilesListAdapter = new FilesListAdapter(getActivity(),
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        String name = file.getName();
                        if (name.equals(".git"))
                            return false;
                        return true;
                    }
                });
        mFilesList.setAdapter(mFilesListAdapter);

        mFilesList
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView,
                            View view, int position, long id) {
                        File file = mFilesListAdapter.getItem(position);
                        if (file.isDirectory()) {
                            setCurrentDir(file);
                            return;
                        }
                        String mime = FsUtils.getMimeType(file);
                        if (FsUtils.isTextMimeType(mime)) {
                            Intent intent = new Intent(getActivity(),
                                    ViewFileActivity.class);
                            intent.putExtra(ViewFileActivity.TAG_FILE_NAME,
                                    file.getAbsolutePath());
                            intent.putExtra(Repo.TAG, mRepo);
                            getRawActivity().startActivity(intent);
                            return;
                        }
                        try {
                            FsUtils.openFile(((SheimiFragmentActivity) getActivity()), file);
                        } catch (ActivityNotFoundException e) {
                            Timber.e(e);
                            ((SheimiFragmentActivity)getActivity()).showMessageDialog(R.string.dialog_error_title,
                                getString(R.string.error_can_not_open_file));
                        }
                    }
                });

        mFilesList
                .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView,
                            View view, int position, long id) {
                        File file = mFilesListAdapter.getItem(position);
                        RepoFileOperationDialog dialog = new RepoFileOperationDialog();
                        Bundle args = new Bundle();
                        args.putString(RepoFileOperationDialog.FILE_PATH,
                                file.getAbsolutePath());
                        dialog.setArguments(args);
                        dialog.show(getFragmentManager(), "repo-file-op-dialog");
                        return true;
                    }
                });

        if (savedInstanceState != null) {
            String currentDirPath = savedInstanceState.getString(CURRENT_DIR);
            if (currentDirPath != null) {
                mCurrentDir = new File(currentDirPath);
                setCurrentDir(mCurrentDir);
            }
        }
        reset();
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Repo.TAG, mRepo);
        if (mCurrentDir != null) {
            outState.putString(CURRENT_DIR, mCurrentDir.getAbsolutePath());
        }
    }

    /**
     * Set the directory listing currently being displayed
     * @param dir
     */
    public void setCurrentDir(File dir) {
        mCurrentDir = dir;
        if (mFilesListAdapter != null) {
            mFilesListAdapter.setDir(mCurrentDir);
        }
        updateBreadcrumb();
    }
    /**
     * 根据当前目录和仓库根目录,生成可点击的面包屑导航
     */
    private void updateBreadcrumb() {
        if (mBreadcrumbContainer == null || mCurrentDir == null || mRootDir == null || getActivity() == null) {
            return;
        }
        mBreadcrumbContainer.removeAllViews();

        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[] { android.R.attr.textColor });
        int textColor = a.getColor(0, Color.BLACK);
        a.recycle();

        // 从当前目录往上收集到仓库根目录,再反转顺序,得到从根到当前的路径链
        List<File> chain = new ArrayList<>();
        File dir = mCurrentDir;
        while (dir != null) {
            chain.add(dir);
            if (dir.equals(mRootDir)) {
                break;
            }
            dir = dir.getParentFile();
        }
        Collections.reverse(chain);

        for (int i = 0; i < chain.size(); i++) {
            final File dirLevel = chain.get(i);
            boolean isLast = (i == chain.size() - 1);
            String label = dirLevel.equals(mRootDir) ? mRepo.getDiaplayName() : dirLevel.getName();
            TextView segment = new TextView(getActivity());
            segment.setText(label);
            segment.setPadding(dpToPx(6), dpToPx(4), dpToPx(6), dpToPx(4));
            segment.setTextColor(isLast ? textColor : Color.parseColor("#1976D2"));
            segment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setCurrentDir(dirLevel);
                }
            });
            mBreadcrumbContainer.addView(segment);

            if (!isLast) {
                TextView separator = new TextView(getActivity());
                separator.setText(" \u203a ");
                separator.setTextColor(Color.parseColor("#9E9E9E"));
                mBreadcrumbContainer.addView(separator);
            }
        }

        mBreadcrumbScroll.post(new Runnable() {
            @Override
            public void run() {
                mBreadcrumbScroll.fullScroll(View.FOCUS_RIGHT);
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * If the root dir has previously been set, set the root dir to be the currently displayed
     * directory listing.
     */
    public void resetCurrentDir() {
        if (mRootDir == null)
            return;
        setCurrentDir(mRootDir);
    }

    @Override
    public void reset() {
        resetCurrentDir();
    }

    public void newDir(String name) {
        File file = new File(mCurrentDir, name);
        if (file.exists()) {
            showToastMessage(R.string.alert_file_exists);
            return;
        }
        file.mkdir();
        setCurrentDir(mCurrentDir);
    }

    /**
     * Create a new file within the currently displayed directory
     *
     * @param name
     */
    public void newFile(String name) throws IOException {
        File file = new File(mCurrentDir, name);
        if (file.exists()) {
            showToastMessage(R.string.alert_file_exists);
            return;
        }
        file.createNewFile();
        setCurrentDir(mCurrentDir);
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
