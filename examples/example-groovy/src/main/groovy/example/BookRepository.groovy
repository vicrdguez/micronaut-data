// tag::repository[]
package example

import io.micronaut.data.annotation.*
import io.micronaut.data.model.*
import io.micronaut.data.repository.CrudRepository

@Repository // <1>
interface BookRepository extends CrudRepository<Book, Long> { // <2>
// end::repository[]

    // tag::simple[]
    Book findByTitle(String title)

    Book getByTitle(String title)

    Book retrieveByTitle(String title)
    // end::simple[]

    // tag::simple-alt[]
    Book find(String title)
    // end::simple-alt[]

    // tag::greaterthan[]
    List<Book> findByPagesGreaterThan(int pageCount)
    // end::greaterthan[]

    // tag::pageable[]
    List<Book> findByPagesGreaterThan(int pageCount, Pageable pageable)

    Page<Book> findByTitleLike(String title, Pageable pageable)

    Slice<Book> list(Pageable pageable)
    // end::pageable[]

    // tag::simple-projection[]
    List<String> findTitleByPagesGreaterThan(int pageCount)
    // end::simple-projection[]

    // tag::top-projection[]
    List<Book> findTop3ByTitleLike(String title)
    // end::top-projection[]

    // tag::ordering[]
    List<Book> listOrderByTitle()

    List<Book> listOrderByTitleDesc()
    // end::ordering[]

    // tag::explicit[]
    @Query("FROM Book b WHERE b.title = :t ORDER BY b.title")
    List<Book> listBooks(String t)
    // end::explicit[]


// tag::repository[]
}
// end::repository[]
