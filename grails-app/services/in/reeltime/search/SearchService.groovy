package in.reeltime.search

import in.reeltime.user.User

class SearchService {

    SearchResult<User> searchForUsers(String query, int page) {
        def result = User.search(query, [reload: true])
        new SearchResult<User>(results: result.results)
    }
}
