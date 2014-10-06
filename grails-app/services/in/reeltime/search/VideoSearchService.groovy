package in.reeltime.search

import in.reeltime.video.Video

class VideoSearchService implements SearchService<Video> {

    @Override
    SearchResult<Video> search(String query, int page){
        def result = Video.search(query, [reload: true, offset: page - 1])
        new SearchResult<Video>(query: query, results: result.results)
    }
}
