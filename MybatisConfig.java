@Configuration
@MapperScan(basePackages = {"com.**.mybatis"})
public class MybatisConfig {

	@Autowired
	ApplicationContext applicationContext;

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        // xml에서 alias사용을 위하여 추가처리
        factoryBean.setConfigLocation(applicationContext.getResource("classpath:mybatis/mybatis-config.xml"));
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        // mapper 위치 설정
        factoryBean.setMapperLocations(resolver.getResources("classpath:mybatis/mapper/*/*.xml"));
        factoryBean.getObject().getConfiguration().setMapUnderscoreToCamelCase(true);
	// sql 데이터 바인딩하여 로그 처리하기 위한 plugins 설정
        MybatisLogInterceptor plugins = new MybatisLogInterceptor();
        /**
         * 다중 plugins 설정 시 사용
        Interceptor[] interceptors = new Interceptor[1];
        interceptors[0] = plugins;
         */
        factoryBean.setPlugins(plugins);
        return factoryBean;
    }
//
//    @Bean
//    public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
//        return new SqlSessionTemplate(sqlSessionFactory);
//    }

}
