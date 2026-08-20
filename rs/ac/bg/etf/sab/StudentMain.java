package rs.ac.bg.etf.sab;

import rs.ac.bg.etf.sab.operations.*;
import rs.ac.bg.etf.sab.tests.TestHandler;
import rs.ac.bg.etf.sab.tests.TestRunner;
import student.*;
public class StudentMain {
    public static void main(String[] args) throws Exception {
        GeneralOperations generalOperations = new md210500_GeneralOperations();
        GenresOperations genresOperations = new md210500_GenresOperations();
        MoviesOperations moviesOperations = new md210500_MoviesOperations();
        RatingsOperations ratingsOperation = new md210500_RatingsOperations();
        TagsOperations tagsOperations = new md210500_TagsOperations();
        UsersOperations usersOperations = new md210500_UsersOperations();
        WatchlistsOperations watchlistsOperations = new md210500_WatchlistsOperations();

        TestHandler.createInstance(
                genresOperations,
                moviesOperations,
                ratingsOperation,
                tagsOperations,
                usersOperations,
                watchlistsOperations,
                generalOperations
        );
        TestRunner.runTests();
    }
}