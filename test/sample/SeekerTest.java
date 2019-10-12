package sample;

import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SeekerTest {

    @BeforeAll
    static void initialize() throws IOException {
//        seeker = new Seeker();
    }

//    @BeforeEach
//    void openUrl() throws IOException {
//        String desktopPath = System.getProperty("user.home") + "/Desktop";
//        bigFile = new File(desktopPath + "/dummy.txt");
//        greatestFile = new File(desktopPath + "/data.log");
//    }
//
//    @Test
//    void searchInString1() {
//        String word = "aa";
//        String text = "aaaaaaaa";
//        assertTrue(seeker.searchInString(text, word));
//    }
//
//    @Test
//    void searchInString2() {
//        String word = "aa";
//        String text = "bbbbbbbbb";
//        assertFalse(seeker.searchInString(text, word));
//    }
//
//    @Test
//    void searchInString3() {
//        String word = "12345678";
//        String text = "123456789";
//        assertTrue(seeker.searchInString(text, word));
//    }
//
//    @Test
//    void searchInString4() {
//        String word = "a";
//        String text = "bbbbbbbba";
//        assertTrue(seeker.searchInString(text, word));
//    }
//
//    @Test
//    void searchInString5() {
//        String word = "123456789";
//        String text = "123456789";
//        assertTrue(seeker.searchInString(text, word));
//    }
//
//

    @RepeatedTest(3)
    void searchSerial() {
        Seeker seeker = new Seeker();
        File bigFile = new File("C:\\$SysReset\\Logs\\setupact.log");
        String text = "Microsoft-Windows-shell32";
        assertTrue(seeker.searchInFileSerial(bigFile, text));
    }

    @RepeatedTest(3)
    void searchParallel() {
        Seeker seeker = new Seeker();
        File bigFile = new File("C:\\$SysReset\\Logs\\setupact.log");
        String text = "Microsoft-Windows-shell32";
        assertTrue(seeker.searchInFileParallel(bigFile, text));
    }

    @Test
    void recursiveSearchParallel() {
        File root = new File(System.getProperty("user.home"));
        Seeker seeker = new Seeker();
        Object result = seeker.searchRecursively(root, "windows", "log", true);
        assertNotNull(result);
    }

    @Test
    void recursiveSearchSerial() {
        File root = new File(System.getProperty("user.home"));
        Seeker seeker = new Seeker();
        Object result = seeker.searchRecursively(root, "windows", "log", false);
        assertNotNull(result);
    }

    @Test
    void JointSearch() {
        File root = new File(System.getProperty("user.home"));
        Seeker seeker = new Seeker(root, "windows", "log");
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(seeker);
        boolean result = seeker.join();
        assertNotNull(result);
    }


////    static File generateFile() {
//////        File file = new File()
////    }
//
////    @AfterAll
////    static void finish() {
////        exe
////    }
//
//    static File greatestFile;
//    static File bigFile;
//    static URL logFile;
//    static Seeker seeker;
}