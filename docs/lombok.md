## Lombok

We use Lombok to auto generate getters/setters and constructors

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

### More information on Lombok

more information on lombok can be found at lombok address here