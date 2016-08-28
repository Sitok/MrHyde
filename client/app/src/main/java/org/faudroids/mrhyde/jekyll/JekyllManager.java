package org.faudroids.mrhyde.jekyll;

import android.content.Context;

import com.google.common.base.Optional;

import org.faudroids.mrhyde.R;
import org.faudroids.mrhyde.git.AbstractNode;
import org.faudroids.mrhyde.git.DirNode;
import org.faudroids.mrhyde.git.FileManager;
import org.faudroids.mrhyde.git.FileNode;
import org.faudroids.mrhyde.git.FileUtils;
import org.faudroids.mrhyde.git.GitManager;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.Observable;
import rx.functions.Func1;
import timber.log.Timber;

/**
 * Handles Jekyll specific tasks for one repository.
 */
public class JekyllManager {

	private static final String
      DIR_NAME_POSTS = "_posts",
			DIR_NAME_DRAFTS = "_drafts";

	private static final Pattern
			POST_TITLE_PATTERN = Pattern.compile("(\\d\\d\\d\\d)-(\\d\\d)-(\\d\\d)(.+)\\..+"),
			DRAFT_TITLE_PATTERN = Pattern.compile("(.+)\\..+");

	private static final DateFormat POST_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private final Context context;
  private final FileUtils fileUtils;
	private final FileManager fileManager;
  private final GitManager gitManager;
  private final File dirPosts, dirDrafts;

  @Deprecated
	JekyllManager(Context context, FileManager fileManager) {
		this.context = context;
		this.fileManager = fileManager;
    this.fileUtils = null;
    this.gitManager = null;
    this.dirPosts = null;
    this.dirDrafts = null;
	}

  JekyllManager(Context context, FileUtils fileUtils, GitManager gitManager) {
    this.context = context;
    this.fileUtils = fileUtils;
    this.gitManager = gitManager;
    this.fileManager = null;
    this.dirPosts = new File(gitManager.getRootDir(), DIR_NAME_POSTS);
    this.dirDrafts = new File(gitManager.getRootDir(), DIR_NAME_DRAFTS);
  }


	/**
	 * Returns all posts sorted by date with the newest first.
	 */
	public Observable<List<Post>> getAllPosts() {
    if (1 == 1) return Observable.just(new ArrayList<>());
		return fileManager.getTree()
				.flatMap(new Func1<DirNode, Observable<List<Post>>>() {
					@Override
					public Observable<List<Post>> call(DirNode dirNode) {
						// check if post dir exists
						List<Post> posts = new ArrayList<>();
						if (!dirNode.getEntries().containsKey(DIR_NAME_POSTS)) return Observable.just(posts);

						// parse titles
						List<FileNode> fileNodes = new ArrayList<>();
						getAllFileNodes((DirNode) dirNode.getEntries().get(DIR_NAME_POSTS), fileNodes);
						for (FileNode fileNode : fileNodes) {
							Optional<Post> post = parsePost(fileNode);
							if (post.isPresent()) posts.add(post.get());
						}

						// sort by date
						Collections.sort(posts);
						Collections.reverse(posts);

						return Observable.just(posts);
					}
				});
	}


	/**
	 * Returns all drafts sorted by title.
	 */
	public Observable<List<Draft>> getAllDrafts() {
    if (1 == 1) return Observable.just(new ArrayList<>());
		return fileManager.getTree()
				.flatMap(new Func1<DirNode, Observable<List<Draft>>>() {
					@Override
					public Observable<List<Draft>> call(DirNode dirNode) {
						// check if drafts dir exists
						List<Draft> drafts = new ArrayList<>();
						if (!dirNode.getEntries().containsKey(DIR_NAME_DRAFTS)) return Observable.just(drafts);

						// parse titles
						List<FileNode> fileNodes = new ArrayList<>();
						getAllFileNodes((DirNode) dirNode.getEntries().get(DIR_NAME_DRAFTS), fileNodes);
						for (FileNode fileNode : fileNodes) {
							Optional<Draft> draft = parseDraft((FileNode) fileNode);
							if (draft.isPresent()) drafts.add(draft.get());
						}

						// sort by title
						Collections.sort(drafts);

						return Observable.just(drafts);
					}
				});
	}


	private void getAllFileNodes(DirNode dirNode, List<FileNode> fileNodes) {
		// parse titles
		for (AbstractNode childNode : dirNode.getEntries().values()) {
			if (childNode instanceof DirNode) {
				getAllFileNodes((DirNode) childNode, fileNodes);
			} else if (childNode instanceof FileNode) {
				fileNodes.add((FileNode) childNode);
			}
		}
	}


	/**
	 * Converts a jekyll post title to the corresponding jekyll filename.
	 */
	public String postTitleToFilename(String title) {
		if (!title.isEmpty()) title = "-" + title;
		return POST_DATE_FORMAT.format(Calendar.getInstance().getTime()) + draftTitleToFilename(title);
	}


	/**
	 * Converts a jekyll draft title to the corresponding filename.
	 */
	public String draftTitleToFilename(String title) {
		return title.replaceAll(" ", "-") + ".md";
	}


	/**
	 * Creates and returns a new post file.
	 */
	public Observable<Post> createNewPost(final String title) {
    return createNewPost(title, dirPosts);
	}


	public Observable<Post> createNewPost(final String title, final File postDir) {
    File targetFile = new File(postDir, postTitleToFilename(title));
    return fileUtils
        .createNewFile(targetFile)
        .flatMap(nothing -> setupDefaultFrontMatter(targetFile, title))
        .map(nothing -> new Post(title, Calendar.getInstance().getTime(), targetFile));
  }


	/**
	 * Creates and returns a new draft file.
	 */
	public Observable<Draft> createNewDraft(final String title) {
    return createNewDraft(title, dirDrafts);
	}


	public Observable<Draft> createNewDraft(final String title, File draftDir) {
    File targetFile = new File(draftDir, draftTitleToFilename(title));
    return fileUtils
        .createNewFile(targetFile)
        .flatMap(nothing -> setupDefaultFrontMatter(targetFile, title))
        .map(nothing -> new Draft(title, targetFile));
	}


	/**
	 * Deletes one {@link AbstractJekyllContent} from the local files system.
	 */
	public <T extends AbstractJekyllContent> Observable<Void> deleteContent(T content) {
		return fileManager.deleteFile(content.getFileNode());
	}


	/**
	 * Publishes a previously created draft to the _posts folder.
	 * @return the newly created post.
	 */
	public Observable<Post> publishDraft(final Draft draft) {
		final String postTitle = postTitleToFilename(draft.getTitle());
		return fileManager.getTree()
				// move draft
				.flatMap(new Func1<DirNode, Observable<FileNode>>() {
					@Override
					public Observable<FileNode> call(DirNode rootDir) {
						DirNode postsDir = assertDir(rootDir, DIR_NAME_POSTS);
						return fileManager.moveFile(draft.getFileNode(), postsDir, postTitle);
					}
				})
				// create post object
				.flatMap(new Func1<FileNode, Observable<Post>>() {
					@Override
					public Observable<Post> call(FileNode newNode) {
						return Observable.just(new Post(
								postTitle,
								Calendar.getInstance().getTime(),
								newNode));
					}
				});
	}


	/**
	 * Moves a previously created post to the _drafts folder.
	 * @return the newly created draft.
	 */
	public Observable<Draft> unpublishPost(final Post post) {
		final String draftTitle = draftTitleToFilename(post.getTitle());
		return fileManager.getTree()
				// move draft
				.flatMap(new Func1<DirNode, Observable<FileNode>>() {
					@Override
					public Observable<FileNode> call(DirNode rootDir) {
						DirNode draftsDir = assertDir(rootDir, DIR_NAME_DRAFTS);
						return fileManager.moveFile(post.getFileNode(), draftsDir, draftTitle);
					}
				})
				// create draft object
				.flatMap(new Func1<FileNode, Observable<Draft>>() {
					@Override
					public Observable<Draft> call(FileNode newNode) {
						return Observable.just(new Draft(draftTitle, newNode));
					}
				});
	}


	/**
	 * Returns true if the passed in dir is the Jekyll posts dir or sub dir.
	 */
	public boolean isPostsDirOrSubDir(File directory) {
		return isSpecialDirOrSubDir(directory, DIR_NAME_POSTS);
	}


	/**
	 * Returns true if the passed in dir is the Jekyll drafts dir or sub dir.
	 */
	public boolean isDraftsDirOrSubDir(File directory) {
		return isSpecialDirOrSubDir(directory, DIR_NAME_DRAFTS);
	}


	private boolean isSpecialDirOrSubDir(File directory, String dirName) {
		File iter = directory;
		while (iter != null) {
			if (iter.getName().equals(dirName)) return true;
      iter = iter.getParentFile();
		}
		return false;
	}


	private DirNode assertDir(DirNode rootNode, String dirName) {
		AbstractNode dir = rootNode.getEntries().get(dirName);
		if (dir == null) {
			dir = fileManager.createNewDir(rootNode, dirName);
		}
		return (DirNode) dir;
	}


	private Observable<Void> setupDefaultFrontMatter(File file, String title) {
    return fileUtils.writeFile(file, context.getString(R.string.default_front_matter, title));
  }



	/**
	 * Removes all local changes and clears all caches..
	 */
	public void resetRepository() {
		fileManager.resetRepository();
	}


  @Deprecated
  public Optional<Post> parsePost(FileNode node) {
    return Optional.absent();
  }

  @Deprecated
  public Optional<Draft> parseDraft(FileNode node) {
    return Optional.absent();
  }

	/**
	 * Tries reading one particular file as a post.
	 */
	public Optional<Post> parsePost(File file) {
		String fileName = file.getName();

		// check for match
		Matcher matcher = POST_TITLE_PATTERN.matcher(fileName);
		if (!matcher.matches()) return Optional.absent();

		try {
			// get date
			int year = Integer.valueOf(matcher.group(1));
			int month = Integer.valueOf(matcher.group(2)) - 1; // java Calendar is 0 based
			int day = Integer.valueOf(matcher.group(3));
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month, day);

			// get title
			String title = formatTitle(matcher.group(4));

			return Optional.of(new Post(title, calendar.getTime(), file));

		} catch (NumberFormatException e) {
			Timber.w(e, "failed to parse post tile \"" + fileName + "\"");
			return Optional.absent();
		}
	}


	/**
	 * Tries reading one particular file as a draft.
	 */
	public Optional<Draft> parseDraft(File file) {
		// check for match
		Matcher matcher = DRAFT_TITLE_PATTERN.matcher(file.getName());
		if (!matcher.matches()) return Optional.absent();

		// get title
		String title = formatTitle(matcher.group(1));

		return Optional.of(new Draft(title, file));
	}


	/**
	 * Removes dashes (-) from a title, replaces those with spaces ( ) and capitalizes
	 * the first character.
	 */
	private String formatTitle(String title) {
		title = title.replaceAll("-", " ");
		title = title.trim();
		title = Character.toUpperCase(title.charAt(0)) + title.substring(1);
		return title;
	}

}
