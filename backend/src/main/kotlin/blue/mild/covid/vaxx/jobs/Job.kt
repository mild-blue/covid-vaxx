package blue.mild.covid.vaxx.jobs

fun interface Job {
    /**
     * Execute the job. It is allowed to throw exception.
     * The job will be executed on the single thread.
     */
    fun execute()
}
