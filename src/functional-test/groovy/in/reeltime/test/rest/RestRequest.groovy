package in.reeltime.test.rest

class RestRequest {
    String url
    String token
    boolean isMultiPart
    Map<String, Object> queryParams = [:]
    Closure customizer
}