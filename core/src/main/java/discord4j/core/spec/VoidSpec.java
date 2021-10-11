package discord4j.core.spec;

public interface VoidSpec extends Spec<Void> {

    @Override
    default Void asRequest() {
        return null;
    }
}
