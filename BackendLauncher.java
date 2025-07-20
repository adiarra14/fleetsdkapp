import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.maxvision.edge.gateway"})
public class BackendLauncher {
    
    public static void main(String[] args) {
        SpringApplication.run(BackendLauncher.class, args);
    }
}
