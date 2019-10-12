package sample;

import com.sun.jdi.BooleanValue;
import javafx.scene.control.TreeItem;

import java.io.*;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class Seeker extends RecursiveTask<Boolean> {

    private File root;
    private String text;
    private String extension;
    private TreeItem<String> treeItem;

    public Seeker() {}

    public Seeker(File root, String text, String extension) {
        this.root = root;
        this.text = text;
        this.extension = extension;
    }

//    public ConcurrentLinkedQueue<File> find(File root, String text, String extension) {
//        result = new ConcurrentLinkedQueue<>();
//        futures = new ConcurrentLinkedQueue<>();
////        Future<?> f = executor.submit(() -> searchInFileSerial(root, text, extension));
////        futures.add(f);
//        searchRecursively(root, text, extension);
//        for (Future<?> future: futures) {
//            try {
//                future.get();
//            } catch (Exception e) {
//                System.out.println(e.toString());
//            }
//        }
//        return result;
//    }


    @Override
    protected Boolean compute() {
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {

                ArrayList<Seeker> seekers = new ArrayList<>(files.length);
                for (File file: files) {
                    seekers.add(new Seeker(file, text, extension));
                }

                invokeAll(seekers);
                seekers.removeIf(seeker -> !seeker.join());

                if (seekers.size() > 0) {
                    treeItem = new TreeItem<>(root.getName());
                    treeItem.setExpanded(true);
                    treeItem.getChildren().addAll(seekers.stream().map(Seeker::getTreeItem).collect(Collectors.toList()));
                    return true;
                }
            }

        } else {
            if (matchByExtension(root, extension) && searchInFileSerial(root, text)) {
                treeItem = new TreeItem<>(root.getName());
                return true;
            }
        }

        return false;
    }

    TreeItem<String> searchRecursively(File root, String text, String extension, boolean parallel) {
        TreeItem<String> node;
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            if (files != null) {
                List<TreeItem<String>> children;

                if (parallel) {
                    children = Arrays
                            .stream(files)
                            .parallel()
                            .map(file -> searchRecursively(file, text, extension, parallel))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                } else {
                    children = new ArrayList<>(files.length);
                    for (File file: files) {
                        TreeItem<String> child = searchRecursively(file, text, extension, parallel);
                        if (child != null)
                            children.add(child);
                    }
                }

                if (children.size() > 0) {
                    node = new TreeItem<>(root.getName());
                    node.setExpanded(true);
                    node.getChildren().addAll(children);
                    return node;
                }
            }

        } else {
            if (matchByExtension(root, extension) && searchInFileSerial(root, text)) {
                return new TreeItem<>(root.getName());
            }
        }

        return null;
    }

//    void searchRecursively(File root, String text, String extension) {
//        if (root.isDirectory()) {
//            File[] files = root.listFiles();
//            if (files != null) {
//                for (File file: files) {
//                    searchRecursively(file, text, extension);
//                }
//            }
//        } else {
//            if (matchByExtension(root, extension)) {
//                System.out.println("Submit");
//                Future<?> future = executor.submit(() -> {
//                    System.out.println("Execute");
//                    try {
//                        if (searchInFileSerial(root, text))
//                            result.add(root);
//                    } catch (IOException e) {
//                        System.out.println(e.toString());
//                    }
//                });
//                futures.add(future);
//            }
//        }
//    }

    boolean searchInFileSerial(File root, String text) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(root));
            String line;

            while ((line = reader.readLine()) != null) {
                if (searchInString(line, text))
                    return true;
            }
            reader.close();
            return false;
        } catch (IOException e){
            System.out.println(e.toString());
            return false;
        }
    }

//    boolean searchInFileSerial(URL url, String text) throws IOException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//        String line;
//
//        while ((line = reader.readLine()) != null) {
//            if (searchInString(line, text))
//                return true;
//        }
//        reader.close();
//        return false;
//    }

    boolean searchInFileParallel(File root, String text) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(root));
            boolean result = reader.lines().parallel().anyMatch(line -> searchInString(line, text));
            reader.close();
            return result;
        } catch (IOException e) {
            return false;
        }
    }

//    boolean searchInFileParallel(URL url, String text) throws IOException {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
//        boolean result = reader.lines().parallel().anyMatch(line -> searchInString(line, text));
//        reader.close();
//        return result;
//    }

    // O(n+m)
//    boolean searchInString(String text, String pattern) {
//        char separator = 0;
//        String concatenated = pattern + separator + text;
//        int[] prefixes = getPrefixes(concatenated);
//        for (int i = 1; i < prefixes.length; i++) {
//            if (prefixes[i] >= pattern.length())
//                return true;
//        }
//        return false;
//    }

    boolean searchInString(String text, String pattern) {
        return getPrefixes(text.toCharArray(), pattern.toCharArray());
    }

    boolean getPrefixes(char[] text, char[] pattern) {
        int[] prefixes = new int[pattern.length];
        prefixes[0] = 0;
        for (int i = 1; i < pattern.length; ++i) {
            int j = prefixes[i - 1];
            while (j > 0 && pattern[i] != pattern[j])
                j = prefixes[j - 1];
            if (pattern[i] == pattern[j])
                ++j;
            prefixes[i] = j;
        }

        for (int i = 0, j = 0; i < text.length; ++i) {
            while (j > 0 && pattern[j] != text[i])
                j = prefixes[j - 1];
            if (pattern[j] == text[i])
                ++j;
            if (j == pattern.length)
                return true;
        }

        return false;
    }

    boolean matchByExtension(File file, String validExtension) {
        if (validExtension.equals(""))
            return true;

        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot == -1)
            return false;

        String extension = name.substring(dot + 1);
        return validExtension.equalsIgnoreCase(extension);
    }

    TreeItem<String> getTreeItem() {
        return treeItem;
    }
}