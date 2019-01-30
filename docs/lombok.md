
## Lombok

We use Lombok to auto generate Getters/Setters and Constructors.

### Getters and Setters

#### Traditional Java

    public class Example {
        private String example;
        
        public String getExample() {
            return example;
        }
        
        public String setExample(String example) {
            this.example = example;
        }
    }

#### Lombok

    public class Example {
        private @Getter @Setter String example;
    }

### Guice

Used in conjunction with Guice for injection eg

    @AllArgsConstructor(onConstructor = @__({ @Inject }))
    
### IntelliJ instructions

1. Download the [IntelliJ Lombok plugin](https://plugins.jetbrains.com/plugin/6317-lombok-plugin)
2. Enable [Annotation Processing](https://www.jetbrains.com/help/idea/configuring-annotation-processing.html)
3. You're good to go!

### More Information

[Project Lombok](https://projectlombok.org)
