package io.github.vooft.spektor.sample.repository

import io.github.vooft.spektor.sample.models.AuthorModel

class AuthorRepository(
    private val authors: MutableList<AuthorModel> = mutableListOf(),
) : List<AuthorModel> by authors {

    fun addAuthor(author: AuthorModel) {
        authors.add(author)
    }
}
