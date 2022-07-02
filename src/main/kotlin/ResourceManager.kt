import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStreamReader

class ResourceManager(
    private val rootDir: String,
    private val useClassLoader: Boolean = false
) {

    private val resource = mutableMapOf<String, List<String>>()

    fun getResource(resourceFileRelativePath: String): List<String> {
        return if(resource.containsKey(resourceFileRelativePath)){
            resource[resourceFileRelativePath]!!.toList()
        }else{
            val result = mutableListOf<String>()

            val br = if(useClassLoader){
                println("load : ${this.javaClass.getResource("$rootDir$resourceFileRelativePath")!!.path!!}")
                val input = this.javaClass.getResourceAsStream("$rootDir$resourceFileRelativePath")!!
                BufferedReader(InputStreamReader(input))
            }else{
                println("load : $rootDir$resourceFileRelativePath")
                val f = FileReader("$rootDir$resourceFileRelativePath")
                BufferedReader(f)
            }


            for(l in br.lines()){
                result.add(l)
            }

            resource[resourceFileRelativePath] = result

            result.toList()
        }
    }
}