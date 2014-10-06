package in.reeltime.search

import in.reeltime.user.User

class UserSearchService implements SearchService<User> {

    SearchResult<User> search(String query, int page) {
        def result = User.search('*' + query + '*', [reload: true, offset: page - 1])
        new SearchResult<User>(query: query, results: result.results)
    }
}
