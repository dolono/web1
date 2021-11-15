@Intercepts({
    @Signature(type=StatementHandler.class, method="update", args={Statement.class})
    , @Signature(type=StatementHandler.class, method="query", args={Statement.class, ResultHandler.class})
})
@Slf4j
public class MybatisLogInterceptor implements Interceptor{

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler handler = (StatementHandler) invocation.getTarget();
		
		BoundSql boundSql = handler.getBoundSql();
		
		// 쿼리문을 가져온다 - 원시쿼리문
		String sql = boundSql.getSql();
		// 파라미터를 가져온다
		Object param = handler.getParameterHandler().getParameterObject();
		
		if(param == null) {		// parameter 가 없는 경우
			sql = sql.replaceFirst("\\?", "''");
		}else {
			if (param instanceof Integer							// 파라미터 형태가 Integer, Long, Float, Double 타입인 경우 
					|| param instanceof Long 
					|| param instanceof Float
					|| param instanceof Double) {
				sql = sql.replaceFirst("\\?", param.toString());
			} else if (param instanceof String) {					// 파라미터 형태가 String 인 경우 - 앞뒤  '(홑따옴표)를 붙여야 하기때문에 별도 처리
				sql = sql.replaceFirst("\\?", "'" + param + "'");
			} else if (param instanceof Map) {						// 파라미터 형태가 Map인 경우
				/**
				 * 쿼리의 ? 와 매핑되는 실제 값들의 정보가 들어잇는 ParameterMapping 객체가 들어간 List 객체로 return
				 * 이때 List 객체의 0번째 순서에 잇는 ParameterMapping 객체가 쿼리의 첫번째 ? 와 매핑
				 * 이런 식으로 쿼리의  ? 와 ParameterMapping 객체를 mapping한다.
				 */
				List<ParameterMapping> paramMapping = boundSql.getParameterMappings();

				for (ParameterMapping mapping : paramMapping) {
					// 파라미터로 넘긴 Map의 Key 값이 들어오게 한다.
					String propValue = mapping.getProperty();
					// 넘겨받은 Key값을 이용해 실제 값을 꺼낸다.
					Object value = ((Map) param).get(propValue);
					// sql의 ? 대신에 실제 값을 넣는다. 이때 String일 경우는 '를 붙어야 하기 때문에 별도 처리
					if (value instanceof String) {
						sql = sql.replaceFirst("\\?", "'" + value + "'");
					} else {
						sql = sql.replaceFirst("\\?", value.toString());
					}

				}
			} else {												// 파라미터가 사용자 정의 클래스 인 경우
				List<ParameterMapping> paramMapping = boundSql.getParameterMappings();

				Class<? extends Object> paramClass = param.getClass();
				for (ParameterMapping mapping : paramMapping) {
					// 해당 파라미터로 넘겨받은 사용자 정의 클래스 객체의 멤버변수명
					String propValue = mapping.getProperty();
					// 관련 멤버변수 Field 객체 얻기
					Field field = paramClass.getDeclaredField(propValue);
					// 멤버변수의 접근자가 private일 경우 reflection을 이용하여 값을 해당 멤버변수의 값을 가져오기 위해 별도 셋팅
					field.setAccessible(true);
					// 해당 파라미터로 넘겨받은 사용자 정의 클래스 객체의 멤버변수의 타입
					Class<?> javaType = mapping.getJavaType();
					
					// sql의 ? 대신에 실제 값을 넣는다. 이때 String일 경우는 '를 붙어야 하기 때문에 별도 처리
					if (String.class == javaType) {
						sql = sql.replaceFirst("\\?", "'" + field.get(param) + "'");
					} else {
						sql = sql.replaceFirst("\\?", field.get(param).toString());
					}

				}
			}
	        	
		}
		
		log.debug("=== Mybatis Parameter Data Binding SQL ===");
		// 첫 줄만 별도로 출력되어 log.debug 미사용
		System.out.println(sql);
		log.debug("==========================================");
		
		return invocation.proceed();	// 쿼리 실행
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

}
