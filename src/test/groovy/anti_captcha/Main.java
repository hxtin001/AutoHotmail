package anti_captcha;

import anti_captcha.Api.ImageToText;
import anti_captcha.Helper.DebugHelper;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        exampleImageToText();
    }

    private static void exampleImageToText() throws InterruptedException {
        DebugHelper.setVerboseMode(true);

        ImageToText api = new ImageToText();
        api.setClientKey("f8228269a43caf2e25ebfafef82515dd");
        api.setFilePath("captcha3.png");

        if (!api.createTask()) {
            DebugHelper.out(
                    "API v2 send failed. " + api.getErrorMessage(),
                    DebugHelper.Type.ERROR
            );
        } else if (!api.waitForResult()) {
            DebugHelper.out("Could not solve the captcha.", DebugHelper.Type.ERROR);
        } else {
            DebugHelper.out("Result: " + api.getTaskSolution().getText(), DebugHelper.Type.SUCCESS);
        }
    }

}
