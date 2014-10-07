package in.reeltime.search

import in.reeltime.reel.Reel

class ReelSearchService implements SearchService<Reel> {

    @Override
    SearchResult<Reel> search(String query, int page){
        def result = Reel.search(query, [reload: true, offset: page - 1])
        new SearchResult<Reel>(query: query, results: result.results)
    }
}
