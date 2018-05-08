import groovy.json.JsonSlurper

class JSONUtils {

    private data
    private fileName = System.getProperty("jsonFileName")

    private parseJSON(String fileName = "data.json") {
        def jsonSlurper = new JsonSlurper()
        def reader

        if(this.fileName?.trim())
        {
            fileName = this.fileName
        }

        reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"UTF-8"))
        data = jsonSlurper.parse(reader)
        return data
    }

    def get(String item) {
        def result = new ArrayList<String>()
        data = parseJSON()
        data.TESTS.each{result.add(it."${item}")}
        return  result
    }

    def getConfig(String item) {
        def result = 0
        data = parseJSON()
        data.CONFIG.each {
            result = it."${item}"
        }
        return result
    }

    def getTestCase() {
        def result = new ArrayList<String>()
        data = parseJSON()
        data.TESTS.eachWithIndex{item, index ->
            result.add(index.toString())
        }
        return  result
    }

}