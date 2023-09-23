import java.util.FormatProcessor;

int count = 10;

void main() {
    String user = FormatProcessor.FMT."""
       %05d\{count}
        """.stripIndent();
    System.out.println(user);
}