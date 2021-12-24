package se.allco.githubbrowser.app.main.repos

import javax.inject.Inject
import se.allco.githubbrowser.R
import se.allco.githubbrowser.utils.ui.recyclerview.DataBoundAdapter

class ReposItemViewModel(val repo: ReposRepository.Repo) : DataBoundAdapter.Item {

    class Factory @Inject constructor() {
        fun create(repo: ReposRepository.Repo): ReposItemViewModel =
            ReposItemViewModel(repo)

        fun create(list: List<ReposRepository.Repo>): List<ReposItemViewModel> =
            list.map { create(it) }
    }

    override fun areItemsTheSame(item: DataBoundAdapter.Item): Boolean =
        (item as ReposItemViewModel).repo.id == repo.id

    override fun areContentsTheSame(item: DataBoundAdapter.Item): Boolean =
        (item as ReposItemViewModel).repo == repo

    override fun getLayoutId(): Int = R.layout.main_repos_item
}
