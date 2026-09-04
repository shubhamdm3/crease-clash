import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import javax.tools.*;

/** Java source launcher: java tools/Build.java [test|smoke|run]. No Gradle needed for desktop. */
class Build {
    public static void main(String[] args) throws Exception {
        JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
        if(compiler==null) throw new IllegalStateException("Install a JDK 17 or newer, not just a JRE.");
        Path output=Path.of("build/classes"); Files.createDirectories(output);
        List<String> options=new ArrayList<>(List.of("--release","17","-encoding","UTF-8","-d",output.toString()));
        for(String root:List.of("core/src/main/java","core/src/test/java","desktop/src/main/java")) {
            try(var paths=Files.walk(Path.of(root))) {
                paths.filter(p->p.toString().endsWith(".java")).forEach(p->options.add(p.toString()));
            }
        }
        int code=compiler.run(null,System.out,System.err,options.toArray(String[]::new));
        if(code!=0) System.exit(code);
        Manifest manifest=new Manifest(); manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,"com.shubham.creaseclash.desktop.DesktopLauncher");
        try(JarOutputStream jar=new JarOutputStream(Files.newOutputStream(Path.of("build/crease-clash-desktop.jar")),manifest)) {
            for(Path root:List.of(output,Path.of("assets"))) {
                try(var paths=Files.walk(root)) {
                    for(Path file:paths.filter(Files::isRegularFile).toList()) {
                        if(file.toString().endsWith(".java")) continue;
                        jar.putNextEntry(new JarEntry(root.relativize(file).toString().replace('\\','/')));
                        Files.copy(file,jar); jar.closeEntry();
                    }
                }
            }
        }
        System.out.println("Compiled Java core, tests and desktop. Built build/crease-clash-desktop.jar");
        String action=args.length==0?"test":args[0];
        List<String> command=new ArrayList<>(List.of(Path.of(System.getProperty("java.home"),"bin","java").toString(),"-cp","build/crease-clash-desktop.jar"));
        if(action.equals("test")) command.add("com.shubham.creaseclash.RulesTest");
        else { command.add("com.shubham.creaseclash.desktop.DesktopLauncher"); if(action.equals("smoke")) command.add("--smoke"); }
        System.exit(new ProcessBuilder(command).inheritIO().start().waitFor());
    }
}
