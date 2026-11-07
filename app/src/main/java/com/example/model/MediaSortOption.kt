package com.example.model

enum class MediaSortOption(val label: String) {
    TITLE_ASC("Title (A–Z)"),
    TITLE_DESC("Title (Z–A)"),
    ARTIST_ASC("Artist / Genre"),
    DURATION_DESC("Duration (Longest)"),
    DURATION_ASC("Duration (Shortest)")
}
