package com.moonanime.app

enum class WatchOrderType {

    PREQUEL,
    MAIN,
    SEQUEL,
    SIDE_STORY,
    ALTERNATIVE,
    OTHER
}

data class WatchOrderEntry(

    val anime: Anime,

    val type: WatchOrderType,

    val position: Int
)

object WatchOrderEngine {

    fun build(
        main: Anime,
        relations: List<AnimeRelation>
    ): List<WatchOrderEntry> {

        val result =
            mutableListOf<WatchOrderEntry>()

        val prequels =
            relations.filter {
                it.type == "PREQUEL"
            }

        val sequels =
            relations.filter {
                it.type == "SEQUEL"
            }

        val sideStories =
            relations.filter {
                it.type == "SIDE_STORY"
            }

        var position = 1

        prequels.forEach {

            result += WatchOrderEntry(
                anime = it.anime,
                type =
                    WatchOrderType.PREQUEL,
                position = position++
            )
        }

        result += WatchOrderEntry(
            anime = main,
            type =
                WatchOrderType.MAIN,
            position = position++
        )

        sequels.forEach {

            result += WatchOrderEntry(
                anime = it.anime,
                type =
                    WatchOrderType.SEQUEL,
                position = position++
            )
        }

        sideStories.forEach {

            result += WatchOrderEntry(
                anime = it.anime,
                type =
                    WatchOrderType.SIDE_STORY,
                position = position++
            )
        }

        return result
    }
}