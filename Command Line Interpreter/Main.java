import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;




public class Main {

    static class Parser {
        String commandName;
        String[] args;

//        public boolean parse(String input) {
//            String[] tokens = input.split(" ");
//            commandName = tokens[0];
//            List<String> argList = new ArrayList<>();
//            StringBuilder currentArg = new StringBuilder();
//            boolean insideQuotes = false;
//
//            for (int i = 1; i < tokens.length; i++) {
//                String token = tokens[i];
//
//                if (insideQuotes) {
//                    currentArg.append(" ").append(token);
//
//                    if (token.endsWith("\"")) {
//                        insideQuotes = false;
//                        argList.add(currentArg.toString().replaceAll("^\"|\"$", ""));
//                        currentArg.setLength(0);
//                    }
//                } else {
//                    if (token.startsWith("\"")) {
//                        if (token.endsWith("\"")) {
//                            argList.add(token.substring(1, token.length() - 1));
//                        } else {
//                            insideQuotes = true;
//                            currentArg.append(token.substring(1));
//                        }
//                    } else {
//                        argList.add(token);
//                    }
//                }
//            }
//
//            args = argList.toArray(new String[0]);
//            return true;
//        }

        public boolean parse(String input)
        {
            String[] words = input.split(" ");
            commandName = words[0];
            args = new String[words.length - 1];
            for (int i = 1; i < words.length; i++) {
                args[i - 1] = words[i];
            }
            return true;
        }

        public String getCommandName() {
            return commandName;
        }

        public String[] getArgs() {
            return args;
        }
    }

    public static class Terminal {
        Parser parser;
        Path currPath = Paths.get("");
        String PathString = currPath.toAbsolutePath().toString();
        ArrayList<String> commandHistory = new ArrayList<>();
        ArrayList<String> argumentsHistory = new ArrayList<>();

        public Terminal(){
            parser = new Parser();
        }

        public void echo(String[] arg) {
            if (arg.length == 0) {
                System.err.println("No arguments provided");
            }
            else if (arg.length > 1)
            {
                System.err.println("Invalid number of arguments");
            }
            else
            {
                System.out.println(arg[0]);
            }
        }

        public void pwd(){

            System.out.println("Current path is : " + PathString);

        }

        public void ls() {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(currPath)) {
                File directory = new File(PathString);
                if (isDirectoryEmpty(directory)) {
                    System.err.println("The directory is empty.");
                } else {
                    for (Path entry : directoryStream) {
                        System.out.print(entry.getFileName() + "     ");
                    }
                    System.out.println();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void ls(String[]args) {
            if(args.length==1&&args[0].equals("-r"))
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(currPath)) {
                    Stack<Path> stack = new Stack<>();
                    for (Path entry : directoryStream) {
                        stack.push(entry.getFileName());
                    }
                    if (stack.isEmpty()) {
                        System.err.println("The directory is empty.");
                    }
                    else
                    {
                        while (!stack.isEmpty()) {
                            Path entry = stack.pop();
                            System.out.print(entry.getFileName() + "     ");
                        }
                        System.out.println();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }

        public void cd(String[] args) {
            if (args.length == 0)
            {
                String home = System.getProperty("user.home");

                System.setProperty("user.dir", home);
                currPath = Paths.get(home);

                PathString = currPath.toAbsolutePath().toString();
                System.out.println("Current path is : " + PathString);
            }
            else if (args.length == 1)
            {
                if (args[0].equals(".."))
                {
                    File currentDirectory = new File(System.getProperty("user.dir"));
                    File parentDirectory = currentDirectory.getParentFile();
                    if (parentDirectory != null && parentDirectory.isDirectory())
                    {
                        System.setProperty("user.dir", parentDirectory.getAbsolutePath());
                        currPath = Paths.get(parentDirectory.getAbsolutePath());
                        PathString = currPath.toAbsolutePath().toString();
                        System.out.println("Current working directory is: " + PathString);
                    }
                    else
                    {
                        System.err.println("Cannot change to the parent directory. It does not exist or is not a directory.");
                    }
                }
                else
                {
                    // Handle the provided path
                    Path newPath = Paths.get(args[0]);

                    if (Files.exists(newPath) && newPath.isAbsolute()) {
                        if (Files.isDirectory(newPath)) {
                            System.setProperty("user.dir", newPath.toAbsolutePath().toString());
                            currPath = newPath;
                            PathString = currPath.toAbsolutePath().toString();
                            System.out.println("Current path is : " + PathString);
                        } else {
                            System.err.println("The provided argument is not a directory.");
                        }
                    } else {
                        System.err.println("The provided argument is not a valid path.");
                    }
                }
            }
            else if (args.length > 1)
            {
                System.err.println("Invalid number of arguments");
            }
            else
            {
                // Handle paths with spaces
                String newPath = String.join(" ", args);
                currPath = Paths.get(newPath);
                PathString = currPath.toAbsolutePath().toString();
                System.out.println("Current path is : " + PathString);
            }
        }

        public void mkdir(String[] args) {
            if (args.length == 0) {
                System.err.println("No arguments provided");
            } else {
                // Get the current directory

                for (String arg : args) {
                    Path newPath;
                    if (Paths.get(arg).isAbsolute()) {
                        newPath = Paths.get(arg);
                    } else {
                        newPath = currPath.resolve(arg);
                    }

                    try {
                        Files.createDirectories(newPath);
                        System.out.println("Created directory: " + newPath.toAbsolutePath());
                    } catch (IOException e) {
                        System.out.println("Failed to create directory: " + newPath.toAbsolutePath());
                        e.printStackTrace();
                    }
                }
            }
        }

        public void rmdir(String[]args) {
            if (args.length == 1) {
                if (args[0].equals("*"))
                {
                    File currentDirectory = new File(PathString);
                    removeEmptyDirectories(currentDirectory);
                }
                else
                {
                    String directoryPath = args[0];
                    File directory = new File(directoryPath);

                    if (!directory.exists())
                    {
                        System.err.println("Directory does not exist: " + directoryPath);// Return an error code
                    }

                    if (!directory.isDirectory()) {
                        System.err.println("Not a directory: " + directoryPath);// Return an error code
                    }

                    if (isDirectoryEmpty(directory)) {
                        if (directory.delete()) {
                            System.out.println("Directory removed: " + directoryPath);
                        } else {
                            System.err.println("Failed to remove directory: " + directoryPath);
                        }
                    } else {
                        System.err.println("Directory is not empty. Cannot remove it.");

                    }
                }
            }
            else if(args.length>1)
            {
                System.err.println("Invalid number of arguments");
            }
            else
            {
                System.err.println("No arguments provided");
            }
        }

        private static boolean isDirectoryEmpty(File directory) { //rmdir(2)
            if (directory.isDirectory()) {
                String[] files = directory.list();
                return files == null || files.length == 0;
            }
            return false;
        }

        public static void removeEmptyDirectories(File directory) { //rmdir(1)
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        removeEmptyDirectories(file);
                    }
                }
                if (directory.list().length == 0) {
                    directory.delete();
                    System.out.println("Removed empty directory: " + directory.getAbsolutePath());
                }
            }
        }

        public void touch(String[] args) {
            Path filePath;
            if (args.length == 0) {
                System.err.println("No arguments provided");
                return;
            }
            else if (args.length == 1)
            {
                if (args[0].startsWith(File.separator)) {
                    // Full path provided
                    filePath = Paths.get(args[0]);
                } else {
                    // Relative (short) path provided
                    filePath = currPath.resolve(args[0]);
                }
                try {
                    Files.createFile(filePath);
                    System.out.println("Created file: " + filePath.toAbsolutePath());
                } catch (IOException e) {
                    System.out.println("Failed to create the file: " + filePath.toAbsolutePath());
                    e.printStackTrace();
                }
            }
            else
            {
                System.err.println("Invalid number of arguments");
            }

        }

        public void cp(String[] args) {
            if (args.length == 2) {
                String sourceFileName = args[0];
                String destinationFileName = args[1];

                try {
                    File sourceFile = new File(sourceFileName);
                    File destinationFile = new File(destinationFileName);

                    if (!sourceFile.exists()) {
                        System.err.println("Source file does not exist.");
                        return;
                    }

                    if (destinationFile.exists()) {
                        System.out.println("Destination file already exists. Overwriting...");
                    }

                    // Create input and output streams
                    FileInputStream inputStream = new FileInputStream(sourceFile);
                    FileOutputStream outputStream = new FileOutputStream(destinationFile);

                    // Create a byte buffer for copying data
                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    // Copy data from the source file to the destination file
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    // Close the streams
                    inputStream.close();
                    outputStream.close();

                    System.out.println("File copied successfully.");
                } catch (IOException e) {
                    System.out.println("An error occurred: " + e.getMessage());
                }
            }

            else if (args.length == 3 && args[0].equals("-r"))
            {
                String sourceDirName = args[1];
                String destinationDirName = args[2];

                File sourceDir = new File(sourceDirName);
                File destinationDir = new File(destinationDirName);

                if (!sourceDir.exists() || !sourceDir.isDirectory()) {
                    System.err.println("Source directory does not exist.");
                    return;
                }

                if (!destinationDir.exists()) {
                    //destinationDir.mkdirs();
                    mkdir(new String[]{destinationDirName});
                }

                try {
                    cp(sourceDir, destinationDir);
                    System.out.println("Directory copied successfully.");
                } catch (IOException e) {
                    System.err.println("An error occurred: " + e.getMessage());
                }
            }

            else {
                System.err.println("Invalid arguments.");
            }


        }

        private static void cp(File sourceDir, File destDir) throws IOException {
            File[] files = sourceDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    File destFile = new File(destDir, file.getName());
                    if (file.isDirectory()) {
                        destFile.mkdirs();
                        cp(file, destFile);
                    } else {
                        Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

        public void rm(String[] args) {
            if (args.length == 1) {
                String FileN = args[0];
                File f = new File(FileN);
                if (!f.exists())
                    System.err.println("File does not exist.");
                else if (f.isDirectory())
                    System.err.println("Cannot remove, file is a directory.");
                else {
                    if (f.delete())
                        System.out.println("File deleted successfully.");
                    else
                        System.err.println("Failed to delete the file.");
                }
            }
            else
            {
                System.err.println("Invalid number of arguments");
            }
        }

        public void cat(String[] args)  {

            if (args.length == 1) {
                File file = new File(args[0]);
                {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading the file: " + file);
                        //e.printStackTrace();
                    }
                }
            }
            else if (args.length==2) {
                File file1 = new File(args[0]);
                File file2 = new File(args[1]);
                try
                {
                    BufferedReader reader1 = new BufferedReader(new FileReader(file1));
                    String line;
                    while ((line = reader1.readLine()) != null) {
                        System.out.println(line);
                    }
                    reader1.close();

                    BufferedReader reader2 = new BufferedReader(new FileReader(file2));
                    while ((line = reader2.readLine()) != null) {
                        System.out.println(line);
                    }
                    reader2.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.err.println("Invalid number of arguments");
            }

        }

        public void history(){
            for (int i=0;i<commandHistory.size()-1;i++)
            {
                System.out.println( i+1 + " - " + commandHistory.get(i)+" "+argumentsHistory.get(i));
            }
        }

        public void chooseCommandAction(String s){

            parser.parse(s);
            String c=parser.getCommandName();
            String []args=parser.getArgs();
            commandHistory.add(c);

            String a ="";
            for (int i =0;i< args.length;i++)
            {
                a+=args[i]+" ";
            }
            argumentsHistory.add(a);

            if ("pwd".equals(c))
            {
                pwd();
            }
            else if ("echo".equals(c))
            {
                echo(args);
            }
            else if ("ls".equals(c)) {
                if (args.length==1)
                {
                    ls(args);

                }
                else if (args.length==0){
                    ls();
                }
                else {
                    System.err.println("Invalid number of arguments");
                }

            }
            else if ("cd".equals(c)) {
                cd(args);
            }
            else if ("mkdir".equals(c)) {
                mkdir(args);
            }
            else if ("rmdir".equals(c)) {
                rmdir(args);
            }
            else if ("touch".equals(c)) {
                touch(args);
            }
            else if ("cp".equals(c)) {
                cp(args);
            }
            else if ("rm".equals(c)){
                rm(args);
            }
            else if ("cat".equals(c)){
                cat(args);
            }
            else if ("history".equals(c)){
                history();
            }
            else
            {
                System.err.println("Command not found");
            }
        }

    }

    public static void  main(String[] args) {
        Terminal t = new Terminal();
        Scanner in = new Scanner(System.in);
        String s = in.nextLine();

        while (!s.equals("exit"))
        {
            t.chooseCommandAction(s);
            s = in.nextLine();
        }
    }
}
//amna

//static class Parser
//    {
//        String commandName;
//        String[] args;
//
//        public boolean parse(String input)
//        {
//            String[] words = input.split(" ");
//            commandName = words[0];
//            args = new String[words.length - 1];
//            for (int i = 1; i < words.length; i++) {
//                args[i - 1] = words[i];
//            }
//            return true;
//        }
//
//        public String getCommandName()
//        {
//            return commandName;
//        }
//
//        public String[] getArgs()
//        {
//            return args;
//        }
//    }