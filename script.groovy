import org.jsoup.Jsoup

def url = args ? args[0] : 'https://www.example.com'
def doc = Jsoup.connect(url).get()

println "Title of $url :"
println doc.title()