package com.app;

public class Resources {
    static final String databaseName = "darshan_books";
    static final String databaseDriverString = "com.mysql.cj.jdbc.Driver";
    static final String databaseConnectionString = "jdbc:mysql://localhost:3306/" + databaseName;
    static final String user = "root";
    static final String password = "comp306";

    static final String reportOneString =
            "SELECT AuthorNationality AS AuthorNationalityGroup, COUNT(AuthorNationality) AS NumberOfBooksWritten" +
            " FROM book INNER JOIN bookauthor" +
            " ON book.AuthorID = bookauthor.AuthorID" +
            " GROUP BY AuthorNationality" +
            " ORDER BY NumberOfBooksWritten DESC";

    static final String reportTwoString =
            "SELECT PublisherName, Count(PublisherName) AS NumberOfBooksInLibraryPublished" +
            " FROM  book INNER JOIN bookpublisher" +
            " ON book.PublisherID = bookpublisher.PublisherID" +
            " GROUP BY PublisherName" +
            " ORDER BY NumberOfBooksInLibraryPublished DESC";

    static final String reportThreeString =
            "SELECT SubjectName, COUNT(SubjectName) AS BooksAboutSubject" +
            " FROM booksubject INNER JOIN book ON" +
            " booksubject.SubjectID = book.SubjectID" +
            " GROUP BY SubjectName" +
            " ORDER BY BooksAboutSubject DESC";
}
