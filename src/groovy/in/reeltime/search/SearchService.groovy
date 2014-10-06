package in.reeltime.search

interface SearchService<DomainClass> {

    SearchResult<DomainClass> search(String query, int page)
}
