
## Lombok

We use Lombok to auto generate Getters/Setters and Constructors.

### Getters and Setters

    public class Example {
        private String example;
        
        public String getExample() {
            return example;
        }
        
        public String setExample(String example) {
            this.example = example;
        }
    }

    public class Example {
        private @Getter @Setter String example;
    }

### Guice

Used in conjunction with Guice for injection eg

    @AllArgsConstructor(onConstructor = @__({ @Inject }))

### More Information

[Project Lombok](https://projectlombok.org)
