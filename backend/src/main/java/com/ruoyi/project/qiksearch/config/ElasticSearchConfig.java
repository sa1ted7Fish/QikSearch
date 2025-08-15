package com.ruoyi.project.qiksearch.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 7.17.X 配置类（适配带rest节点的认证配置）
 */
@Configuration
public class ElasticSearchConfig {

    // 从yml中读取ES地址
    @Value("${spring.elasticsearch.uris}")
    private String uris;

    // 从yml的rest节点读取用户名和密码
    @Value("${spring.elasticsearch.rest.username}")
    private String username;

    @Value("${spring.elasticsearch.rest.password}")
    private String password;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        // 创建凭证提供器（ES 7.17.X兼容的4.x版本HttpClient写法）
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,  // 匹配所有地址和端口
                new UsernamePasswordCredentials(username, password)  // 传入用户名和密码
        );

        // 解析ES地址（支持集群多地址）
        String[] hostArray = uris.split(",");
        HttpHost[] httpHosts = new HttpHost[hostArray.length];
        for (int i = 0; i < hostArray.length; i++) {
            String host = hostArray[i];
            try {
                // 解析地址中的协议、主机和端口
                java.net.URI uri = new java.net.URI(host);
                httpHosts[i] = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
            } catch (Exception e) {
                throw new RuntimeException("解析ES地址失败: " + host, e);
            }
        }

        // 构建带认证的RestClient
        RestClientBuilder builder = RestClient.builder(httpHosts)
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                );

        // 创建并返回高级客户端
        return new RestHighLevelClient(builder);
    }
}
