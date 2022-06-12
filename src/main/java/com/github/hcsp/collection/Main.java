package com.github.hcsp.collection;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
//import sun.nio.cs.ext.EUC_CN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.moduleLabelInPackage;
//import static jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle.title;

public class Main {
    public static void main(String[] args) throws RuntimeException, IOException {
        //待处理连接池
        List<String> linkpool = new ArrayList<>();
        //判断是不是在连接池里面，用Set，此处为处理过后的连接池
        Set<String > processedLinks =new HashSet<>();
        linkpool.add("https://sina.cn");
        while (true) {
            if (linkpool.isEmpty()){
                break;
            }
            String link = linkpool.remove(linkpool.size()-1);//拿最后一个,remove会返回刚刚删除的元素
         //处理完删掉，第二次判断要从池子中拿走
            if (processedLinks.contains(link)){
                continue;
            }
            //if ((link.contains("news.sina.cn") || "https://sina.cn".equals(link))) {
            if(isInterestingLink(link))  {  

                Document doc = httpGetAndParseHtml(link);

                doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkpool::add);//把用map把一个映射成另一个,对每一个都add连接池

                //加入这是一个新闻页面，则写入数据库，否则不做,标题为空
                storeIntoDatabaseIfitisnewspage(doc);

                processedLinks.add(link);
            } else {
                //这不是我们想要处理的
            }
        }
        }

    private static void storeIntoDatabaseIfitisnewspage(Document doc) {
        ArrayList<Element> articlTags = doc.select("article");
        if (!articlTags.isEmpty()){         //从所有标签拿title
            for (Element articleTag : articlTags){//把A标签打印出来
                String title = articlTags.get(0).child(0).text();
                System.out.println(title);
            }
        }
    }

    private static Document httpGetAndParseHtml(String link) {
        //这是我们想处理的（以sina.cn结尾），就是拿到数据（用jsoup)
        CloseableHttpClient httpClient = HttpClients.createDefault();
        System.out.println(link);

        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36");

        try (CloseableHttpResponse response1 = httpClient.execute(httpGet)){
            System.out.println(response1.getStatusLine());
            HttpEntity entity1 =response1.getEntity();
            String html=EntityUtils.toString(entity1);
            //jsoup 是一款Java 的HTML解析器
            return  Jsoup.parse(html);
            //element 继承于arraylist

        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isInterestingLink(String link) {
        return (isNewsPages(link) || "https://sina.cn".equals(link))&&isNotLoginPage(link);
    }
    private static boolean isIndexPage(String link){
        return "https://sina.cn".equals(link);
    }
    private static boolean isNewsPages(String link){
        return link.contains("news.sina.cn");
    }
    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }
}
