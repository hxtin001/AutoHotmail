import spock.util.EmbeddedSpecRunner

class HotmailRegisterMain {

    static void main(String[] args) {
        def ignoring = { Class<? extends Throwable> catchMe, Closure callMe ->
            try {
                callMe.call()
            } catch(e) {
                if (!e.class.isAssignableFrom(catchMe)) {
                    throw e
                }
            }
        }
        EmbeddedSpecRunner embeddedSpecRunner = new EmbeddedSpecRunner()
        ignoring(org.openqa.selenium.NoSuchSessionException){
            embeddedSpecRunner.runClass(HotmailSpec.class)
        }

    }

}
