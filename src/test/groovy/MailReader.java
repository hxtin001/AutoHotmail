import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MailReader {

    public MailReader() {
    }

    public ArrayList<String> getMails(String path) throws IOException {
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
        ArrayList<String> mailList = new ArrayList<String>();
        String strLine;
        while ((strLine = bufferReader.readLine()) != null)   {
            if (!strLine.trim().isEmpty()) {
                mailList.add(strLine);
            }
        }
        return mailList;
    }

}
