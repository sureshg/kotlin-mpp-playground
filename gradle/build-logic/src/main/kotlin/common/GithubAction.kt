package common

import java.lang.System.getenv
import java.nio.file.StandardOpenOption.*
import java.util.*
import kotlin.io.path.*

/**
 * Workflow commands for GitHub Actions.
 *
 * [WorkflowCommands](https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions)
 * for more details.
 */
object GithubAction {

  /** Returns `true` if the running on GitHub action workflow. */
  val isEnabled = getenv("GITHUB_ACTIONS").toBoolean()

  /** Return workflow run's URL */
  val workflowRunURL
    get() =
        when {
          isEnabled ->
              "${Env.GITHUB_SERVER_URL}/${Env.GITHUB_REPOSITORY}/actions/runs/${Env.GITHUB_RUN_ID}"
          else -> null
        }

  /**
   * Returns a boolean value indicating whether the current build is a tag build. A tag build refers
   * to a build triggered by a git tag push event.
   *
   * @return true if the build is a tag build, false otherwise.
   */
  val isTagBuild
    get() = isEnabled && Env.GITHUB_REF.startsWith("refs/tags/")

  /**
   * Returns the ref name (tag/branch) that triggered the GitHub workflow run.
   *
   * @return The ref name.
   */
  val ref
    get() =
        when (isTagBuild) {
          true -> Env.GITHUB_REF?.substringAfter("refs/tags/")
          false -> Env.GITHUB_REF?.substringAfter("refs/heads/")
        }

  /**
   * Prints a debug message to the log. You must create a secret named **ACTIONS_STEP_DEBUG** with
   * the value `true` to see the debug messages set by this command in the log.
   */
  fun debug(message: String) = echo(message(message, MsgType.DEBUG))

  /**
   * Creates a notice message and prints the message to the log. This message will create an
   * annotation, which can associate the message with a particular file in your repository.
   * Optionally, your message can specify a position within the file.
   */
  fun notice(
      message: String,
      title: String = "",
      file: String = "",
      line: Int = 0,
      endLine: Int = 0,
      col: Int = 0,
      endColumn: Int = 0
  ) =
      echo(
          message(
              message = message,
              type = MsgType.NOTICE,
              title = title,
              file = file,
              line = line,
              endLine = endLine,
              col = col,
              endColumn = endColumn))

  /**
   * Creates a warning message and prints the message to the log. This message will create an
   * annotation, which can associate the message with a particular file in your repository.
   * Optionally, your message can specify a position within the file.
   */
  fun warning(
      message: String,
      title: String = "",
      file: String = "",
      line: Int = 0,
      endLine: Int = 0,
      col: Int = 0,
      endColumn: Int = 0
  ) =
      echo(
          message(
              message = message,
              type = MsgType.WARNING,
              title = title,
              file = file,
              line = line,
              endLine = endLine,
              col = col,
              endColumn = endColumn))

  /**
   * Creates an error message and prints the message to the log. This message will create an
   * annotation, which can associate the message with a particular file in your repository.
   * Optionally, your message can specify a position within the file.
   */
  fun error(
      message: String,
      title: String = "",
      file: String = "",
      line: Int = 0,
      endLine: Int = 0,
      col: Int = 0,
      endColumn: Int = 0
  ) =
      echo(
          message(
              message = message,
              type = MsgType.ERROR,
              title = title,
              file = file,
              line = line,
              endLine = endLine,
              col = col,
              endColumn = endColumn))

  /** Creates an expandable group with a title in the log. */
  fun group(title: String, logs: List<String>) {
    if (isEnabled) {
      println("::group::$title")
      logs.forEach(::println)
      println("::endgroup::")
    }
  }

  /** Masking a string. Masked word separated by space is replaced with the `*` character. */
  fun mask(message: String) = "::add-mask::$message"

  /** Prints message to the GitHub Action workflow log. */
  fun echo(message: String, mask: Boolean = false) {
    if (isEnabled) {
      // Replace line feed in multiline strings.
      val msg =
          when (mask) {
            true -> mask(message)
            else -> message
          }.replace("\\R", "%0A")
      println(msg)
    }
  }

  /**
   * Stops processing any workflow commands. This special command allows you to log anything without
   * accidentally running a workflow command.
   */
  fun stopCommand(messages: List<String>) {
    if (isEnabled) {
      val token = UUID.randomUUID().toString()
      println(
          """
          |::stop-commands::$token
          |${messages.joinToString(System.lineSeparator())}
          |::$token::
          """
              .trimMargin())
    }
  }

  /**
   * Setting an environment variable available to any subsequent steps in a workflow.The step that
   * creates or updates the environment variable does not have access to the new value, but all
   * subsequent steps in a job will have access. The names of environment variables are
   * case-sensitive, and you can include punctuation.
   */
  fun setEnv(name: String, value: String) {
    val env =
        when (value.lines().size > 1) {
          // Multiline string
          true ->
              """
              |$name<<EOF
              |$value
              |EOF
              """
                  .trimMargin()
          else -> "$name=$value"
        }
    writeEnvFile("GITHUB_ENV", env)
  }

  /**
   * Prepends a directory to the system `PATH` variable and automatically makes it available to all
   * subsequent actions in the current job. The currently running action cannot access the updated
   * path variable. To see the currently defined paths for your job, you can use **echo "$PATH"** in
   * a step or an action.
   */
  fun addPath(path: String) = writeEnvFile("GITHUB_PATH", path)

  /** Sets a GitHub Action's output parameter. */
  fun setOutput(name: String, value: Any) = writeEnvFile("GITHUB_OUTPUT", "$name=$value")

  /** Returns the workflow input with the given [name]. */
  fun getInput(name: String): String? = getenv("INPUT_$name".uppercase())

  /** Returns the workflow state with the given [name]. */
  fun getState(name: String): String? = getenv("STATE_$name".uppercase())

  /**
   * Create environment variables for sharing with your workflow's `pre:` or `post:` actions by
   * writing to the file located at `GITHUB_STATE`. For example, you can create a file with the pre:
   * action, pass the file location to the main: action, and then use the post: action to delete the
   * file.
   */
  fun saveState(name: String, value: Any) = writeEnvFile("GITHUB_STATE", "$name=$value")

  /**
   * Set some custom Markdown for each job so that it will be displayed on the summary page of a
   * workflow run.
   */
  fun addJobSummary(gfmContent: String, overwrite: Boolean = false) =
      writeEnvFile("GITHUB_STEP_SUMMARY", gfmContent, overwrite)

  /** Get the current job summary string. */
  fun getJobSummary(): String {
    val jonSummaryFile = getenv("GITHUB_STEP_SUMMARY")
    return when (jonSummaryFile != null) {
      true -> Path(jonSummaryFile).readText()
      else -> ""
    }
  }

  /** Completely remove a summary for the current step */
  fun removeJobSummary() = Path(getenv("GITHUB_STEP_SUMMARY")).deleteIfExists()

  /** Append the [value] string with newline to file returned by the [env] variable. */
  private fun writeEnvFile(env: String, value: String, truncate: Boolean = false) {
    if (isEnabled && env.isNotBlank()) {
      val ghActionEnv = getenv(env)
      if (ghActionEnv != null) {
        debug("Writing to Github Action '$env' file: $ghActionEnv, truncate: $truncate")
        Path(ghActionEnv)
            .writeText(
                text = "$value${System.lineSeparator()}",
                charset = Charsets.UTF_8,
                CREATE,
                if (truncate) TRUNCATE_EXISTING else APPEND,
                WRITE)
      }
    }
  }

  /**
   * Creates a GitHub Action workflow message to log. The message (except **DEBUG**) will create an
   * annotation, which can associate the message with a particular file in your repository.
   * Optionally, your message can specify a position within the file.
   *
   * @param message message string
   * @param type message type [MsgType]
   * @param title Custom title
   * @param file Filename
   * @param col Column number, starting at 1
   * @param endColumn End column number
   * @param line Line number, starting at 1
   * @param endLine End line number
   */
  private fun message(
      message: String,
      type: MsgType,
      title: String = "",
      file: String = "",
      line: Int = 0,
      endLine: Int = 0,
      col: Int = 0,
      endColumn: Int = 0
  ) = buildString {
    append("::")
    append(type.name.lowercase())
    when {
      type != MsgType.DEBUG -> {
        val params = mutableListOf<String>()
        if (title.isNotBlank()) {
          params.add("title=$title")
        }
        if (file.isNotBlank()) {
          params.add("file=$file")
          if (line > 0) params.add("line=$line")
          if (endLine > 0) params.add("endLine=$endLine")
          if (col > 0) params.add("col=$col")
          if (endColumn > 0) params.add("endColumn=$endColumn")
        }
        append(" ")
        append(params.joinToString(","))
      }
    }
    append("::")
    append(message)
  }

  internal enum class MsgType {
    DEBUG,
    NOTICE,
    WARNING,
    ERROR
  }

  /**
   * The default environment variables that GitHub sets are available to every step in a workflow.
   * Environment variables are case-sensitive. Environment variables are always interpolated on the
   * virtual machine runner. However, parts of a workflow are processed by GitHub Actions and are
   * not sent to the runner. You cannot use environment variables in these parts of a workflow file.
   * Instead, you can use contexts. For example, an if conditional, which determines whether a job
   * or step is sent to the runner, is always processed by GitHub Actions. You can use a context in
   * an if conditional statement to access the value of an environment variable (**${{
   * env.MY_VARIABLE }}**).
   *
   * Most of the default environment variables have a corresponding, and similarly named, context
   * property. For example, the value of the **GITHUB_REF** environment variable can be read during
   * workflow processing using the **${{ github.ref }}** context property.
   *
   * See
   * [EnvVars](https://docs.github.com/en/actions/learn-github-actions/environment-variables#default-environment-variables)
   */
  object Env {

    /** Always set to true. */
    val CI
      get() = getenv("CI").toBoolean()

    /**
     * The name of the action currently running, or the id of a step. For example, for an action,
     * __repo-owner_name-of-action-repo.GitHub removes special characters, and uses the name __run
     * when the current step runs a script without an id. If you use the same script or action more
     * than once in the same job, the name will include a suffix that consists of the sequence
     * number preceded by an underscore. For example, the first script you run will have the name
     * __run, and the second script will be named __run_2. Similarly, the second invocation of
     * actions/checkout will be actionscheckout2.
     */
    val GITHUB_ACTION
      get() = getenv("GITHUB_ACTION")

    /**
     * The path where an action is located. This property is only supported in composite actions.
     * You can use this path to change directories to where the action is located and access other
     * files in that same repository. For example,
     * /home/runner/work/_actions/repo-owner/name-of-action-repo/v1.
     */
    val GITHUB_ACTION_PATH
      get() = getenv("GITHUB_ACTION_PATH")

    /**
     * For a step executing an action, this is the owner and repository name of the action. For
     * example, actions/checkout.
     */
    val GITHUB_ACTION_REPOSITORY
      get() = getenv("GITHUB_ACTION_REPOSITORY")

    /**
     * Always set to true when GitHub Actions is running the workflow. You can use this variable to
     * differentiate when tests are being run locally or by GitHub Actions.
     */
    val GITHUB_ACTIONS
      get() = getenv("GITHUB_ACTIONS")

    /** The name of the person or app that initiated the workflow. For example, octocat. */
    val GITHUB_ACTOR
      get() = getenv("GITHUB_ACTOR")

    /**
     * The account ID of the person or app that triggered the initial workflow run. For
     * example, 1234567. Note that this is different from the actor username.
     */
    val GITHUB_ACTOR_ID
      get() = getenv("GITHUB_ACTOR_ID")

    /** Returns the API URL. For example: https://api.github.com. */
    val GITHUB_API_URL
      get() = getenv("GITHUB_API_URL")

    /**
     * The name of the base ref or target branch of the pull request in a workflow run. This is only
     * set when the event that triggers a workflow run is either pull_request or
     * pull_request_target. For example, main.
     */
    val GITHUB_BASE_REF
      get() = getenv("GITHUB_BASE_REF")

    /**
     * The path on the runner to the file that sets variables from workflow commands. This file is
     * unique to the current step and changes for each step in a job. For example,
     * /home/runner/work/_temp/_runner_file_commands/set_env_87406d6e-4979-4d42-98e1-3dab1f48b13a.
     * For more information, see "Workflow commands for GitHub Actions."
     */
    val GITHUB_ENV
      get() = getenv("GITHUB_ENV")

    /** The name of the event that triggered the workflow. For example, workflow_dispatch. */
    val GITHUB_EVENT_NAME
      get() = getenv("GITHUB_EVENT_NAME")

    /**
     * The path to the file on the runner that contains the full event webhook payload. For example,
     * /github/workflow/event.json.
     */
    val GITHUB_EVENT_PATH
      get() = getenv("GITHUB_EVENT_PATH")

    /** Returns the GraphQL API URL. For example: https://api.github.com/graphql. */
    val GITHUB_GRAPHQL_URL
      get() = getenv("GITHUB_GRAPHQL_URL")

    /**
     * The head ref or source branch of the pull request in a workflow run. This property is only
     * set when the event that triggers a workflow run is either pull_request or
     * pull_request_target. For example, feature-branch-1.
     */
    val GITHUB_HEAD_REF
      get() = getenv("GITHUB_HEAD_REF")

    /** The job_id of the current job. For example, greeting_job. */
    val GITHUB_JOB
      get() = getenv("GITHUB_JOB")

    /**
     * The path on the runner to the file that sets the current step's outputs from workflow
     * commands. This file is unique to the current step and changes for each step in a job. For
     * example,
     * /home/runner/work/_temp/_runner_file_commands/set_output_a50ef383-b063-46d9-9157-57953fc9f3f0.
     * For more information, see "Workflow commands for GitHub Actions."
     */
    val GITHUB_OUTPUT
      get() = getenv("GITHUB_OUTPUT")

    /**
     * The path on the runner to the file that sets system PATH variables from workflow commands.
     * This file is unique to the current step and changes for each step in a job. For example,
     * /home/runner/work/_temp/_runner_file_commands/add_path_899b9445-ad4a-400c-aa89-249f18632cf5.
     * For more information, see "Workflow commands for GitHub Actions."
     */
    val GITHUB_PATH
      get() = getenv("GITHUB_PATH")

    /**
     * The fully-formed ref of the branch or tag that triggered the workflow run. For workflows
     * triggered by push, this is the branch or tag ref that was pushed. For workflows triggered by
     * pull_request, this is the pull request merge branch. For workflows triggered by release, this
     * is the release tag created. For other triggers, this is the branch or tag ref that triggered
     * the workflow run. This is only set if a branch or tag is available for the event type. The
     * ref given is fully-formed, meaning that for branches the format is refs/heads/<branch_name>,
     * for pull requests it is refs/pull/<pr_number>/merge, and for tags it is refs/tags/<tag_name>.
     * For example, refs/heads/feature-branch-1.
     */
    val GITHUB_REF
      get() = getenv("GITHUB_REF")

    /**
     * The short ref name of the branch or tag that triggered the workflow run. This value matches
     * the branch or tag name shown on GitHub. For example, feature-branch-1. For pull requests, the
     * format is refs/pull/<pr_number>/merge.
     */
    val GITHUB_REF_NAME
      get() = getenv("GITHUB_REF_NAME")

    /**
     * true if branch protections or rulesets are configured for the ref that triggered the workflow
     * run.
     */
    val GITHUB_REF_PROTECTED
      get() = getenv("GITHUB_REF_PROTECTED")

    /** The type of ref that triggered the workflow run. Valid values are branch or tag. */
    val GITHUB_REF_TYPE
      get() = getenv("GITHUB_REF_TYPE")

    /** The owner and repository name. For example, octocat/Hello-World. */
    val GITHUB_REPOSITORY
      get() = getenv("GITHUB_REPOSITORY")

    /**
     * The ID of the repository. For example, 123456789. Note that this is different from the
     * repository name.
     */
    val GITHUB_REPOSITORY_ID
      get() = getenv("GITHUB_REPOSITORY_ID")

    /** The repository owner's name. For example, octocat. */
    val GITHUB_REPOSITORY_OWNER
      get() = getenv("GITHUB_REPOSITORY_OWNER")

    /**
     * The repository owner's account ID. For example, 1234567. Note that this is different from the
     * owner's name.
     */
    val GITHUB_REPOSITORY_OWNER_ID
      get() = getenv("GITHUB_REPOSITORY_OWNER_ID")

    /** The number of days that workflow run logs and artifacts are kept. For example, 90. */
    val GITHUB_RETENTION_DAYS
      get() = getenv("GITHUB_RETENTION_DAYS")

    /**
     * A unique number for each attempt of a particular workflow run in a repository. This number
     * begins at 1 for the workflow run's first attempt, and increments with each re-run. For
     * example, 3.
     */
    val GITHUB_RUN_ATTEMPT
      get() = getenv("GITHUB_RUN_ATTEMPT")

    /**
     * A unique number for each workflow run within a repository. This number does not change if you
     * re-run the workflow run. For example, 1658821493.
     */
    val GITHUB_RUN_ID
      get() = getenv("GITHUB_RUN_ID")

    /**
     * A unique number for each run of a particular workflow in a repository. This number begins at
     * 1 for the workflow's first run, and increments with each new run. This number does not change
     * if you re-run the workflow run. For example, 3.
     */
    val GITHUB_RUN_NUMBER
      get() = getenv("GITHUB_RUN_NUMBER")

    /** The URL of the GitHub server. For example: https://github.com. */
    val GITHUB_SERVER_URL
      get() = getenv("GITHUB_SERVER_URL")

    /**
     * The commit SHA that triggered the workflow. The value of this commit SHA depends on the event
     * that triggered the workflow. For more information, see "Events that trigger workflows." For
     * example, ffac537e6cbbf934b08745a378932722df287a53.
     */
    val GITHUB_SHA
      get() = getenv("GITHUB_SHA")

    /**
     * The path on the runner to the file that contains job summaries from workflow commands. This
     * file is unique to the current step and changes for each step in a job. For example,
     * /home/runner/_layout/_work/_temp/_runner_file_commands/step_summary_1cb22d7f-5663-41a8-9ffc-13472605c76c.
     * For more information, see "Workflow commands for GitHub Actions."
     */
    val GITHUB_STEP_SUMMARY
      get() = getenv("GITHUB_STEP_SUMMARY")

    /**
     * The username of the user that initiated the workflow run. If the workflow run is a re-run,
     * this value may differ from github.actor. Any workflow re-runs will use the privileges of
     * github.actor, even if the actor initiating the re-run (github.triggering_actor) has different
     * privileges.
     */
    val GITHUB_TRIGGERING_ACTOR
      get() = getenv("GITHUB_TRIGGERING_ACTOR")

    /**
     * The name of the workflow. For example, My test workflow. If the workflow file doesn't specify
     * a name, the value of this variable is the full path of the workflow file in the repository.
     */
    val GITHUB_WORKFLOW
      get() = getenv("GITHUB_WORKFLOW")

    /**
     * The ref path to the workflow. For example,
     * octocat/hello-world/.github/workflows/my-workflow.yml@refs/heads/my_branch.
     */
    val GITHUB_WORKFLOW_REF
      get() = getenv("GITHUB_WORKFLOW_REF")

    /** The commit SHA for the workflow file. */
    val GITHUB_WORKFLOW_SHA
      get() = getenv("GITHUB_WORKFLOW_SHA")

    /**
     * The default working directory on the runner for steps, and the default location of your
     * repository when using the checkout action. For example,
     * /home/runner/work/my-repo-name/my-repo-name.
     */
    val GITHUB_WORKSPACE
      get() = getenv("GITHUB_WORKSPACE")

    /**
     * The architecture of the runner executing the job. Possible values are X86, X64, ARM, or
     * ARM64.
     */
    val RUNNER_ARCH
      get() = getenv("RUNNER_ARCH")

    /**
     * This is set only if debug logging is enabled, and always has the value of 1. It can be useful
     * as an indicator to enable additional debugging or verbose logging in your own job steps.
     */
    val RUNNER_DEBUG
      get() = getenv("RUNNER_DEBUG") == "1"

    /**
     * The name of the runner executing the job. This name may not be unique in a workflow run as
     * runners at the repository and organization levels could use the same name. For example,
     * Hosted Agent
     */
    val RUNNER_NAME
      get() = getenv("RUNNER_NAME")

    /**
     * The operating system of the runner executing the job. Possible values are Linux, Windows, or
     * macOS. For example, Windows
     */
    val RUNNER_OS
      get() = getenv("RUNNER_OS")

    /**
     * The path to a temporary directory on the runner. This directory is emptied at the beginning
     * and end of each job. Note that files will not be removed if the runner's user account does
     * not have permission to delete them. For example, D:\a\_temp
     */
    val RUNNER_TEMP
      get() = getenv("RUNNER_TEMP")

    /**
     * The path to the directory containing preinstalled tools for GitHub-hosted runners. For more
     * information, see "Using GitHub-hosted runners". For example, C:\hostedtoolcache\windows
     */
    val RUNNER_TOOL_CACHE
      get() = getenv("RUNNER_TOOL_CACHE")

    /**
     * Default location of your repository when using the checkout action. Can access like `${{
     * env.RUNNER_WORKSPACE }}` from GitHub action YAML.
     */
    val RUNNER_WORKSPACE
      get() = getenv("RUNNER_WORKSPACE")

    /**
     * GitHub runner context. Contexts are a way to access information about workflow runs, runner
     * environments, jobs, and steps. Each context is an object that contains properties, which can
     * be strings or other objects.
     */
    val RUNNER_CONTEXT
      get() = getenv("RUNNER_CONTEXT")

    fun javaHomeFor(version: Int = 21) =
        when (version) {
          8,
          11,
          17,
          21 -> {
            val arch = if (RUNNER_ARCH == "ARM64") RUNNER_ARCH.lowercase() else RUNNER_ARCH
            getenv("JAVA_HOME_${version}_${arch}")
          }
          else -> throw IllegalArgumentException("Unsupported Java version: $version")
        }

    /** Gets the value of the environment variable set in the Github action runner. */
    operator fun get(name: String): String? = getenv(name)
  }
}
