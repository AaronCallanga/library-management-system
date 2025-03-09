package com.system.libraryManagementSystem.service;

import com.system.libraryManagementSystem.exception.AuthorNotFoundException;
import com.system.libraryManagementSystem.model.Author;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.repository.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    //getAllAuthors_WhenInvalidSortField_ShouldThrowException()
    //getAuthorsByPublishedBookTitle_WhenAuthorHasNoBooks_ShouldReturnEmpty()
    //getAllAuthors_WhenLastPageRequested_ShouldReturnRemainingAuthorsOnly()
    //implement parameterized test
    @Mock //create a mock object so we can use the repo
    private AuthorRepository authorRepository;

    @InjectMocks  //whatever the dependencies this class needed(the repository) will be injected, and this service will become mock object so we can use it too
    private AuthorService authorService;

    private Author author1;
    private Author author2;
    private PageRequest  pageRequest;
    private List<Author> authorList;

    @BeforeEach
    void buildAuthor() {
        author1 = Author.builder()
                .id(1L)
                .name("J.K. Rowling")
                .biography("British author best known for the Harry Potter series.")
                .publishedBooks(new ArrayList<>())
                .build();

        author2 = Author.builder()
                .id(2L)
                .name("George Orwell")
                .biography("English novelist, best known for '1984' and 'Animal Farm'.")
                .publishedBooks(new ArrayList<>())
                .build();

        Book book1 = new Book(1L, "Harry Potter and the Philosopher's Stone", "Fantasy", 1997, author1, new ArrayList<>());
        Book book2 = new Book(2L, "Harry Potter and the Chamber of Secrets", "Fantasy", 1998, author1, new ArrayList<>());
        Book book3 = new Book(3L, "1984", "Dystopian", 1949, author2, new ArrayList<>());
        Book book4 = new Book(4L, "Animal Farm", "Political Satire", 1945, author2, new ArrayList<>());

        author1.setPublishedBooks(List.of(book1, book2));
        author2.setPublishedBooks(List.of(book3, book4));

        pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.fromString("ASC"), "id"));
        authorList = Arrays.asList(author1, author2);
    }


    @Test
    void getAllAuthors_WhenListOfAuthorExist_ShouldReturnPageOfAuthors() {
        //arrange
        Page<Author> authorPage = new PageImpl<>(authorList, pageRequest, authorList.size());   //we also passed pageRequest and authorList.size to retain metadate info for assertion
        //mock repository behavior
        when(authorRepository.findAll(pageRequest)).thenReturn(authorPage);

        //act
        Page<Author> result = authorService.getAllAuthors(0,10,"ASC", "id");

        //assert
        assertNotNull(result);
        assertIterableEquals(authorList, result);
        assertEquals(authorPage.getTotalElements(), result.getTotalElements());
        assertEquals(2, result.getContent().size());    //test the size
        assertTrue(result.getSort().isSorted());    //test if it is sorted, whether asc or desc
        assertEquals("id: ASC", result.getSort().toString());    //test sortfield and sortdirection
        assertEquals(1L, result.getContent().get(0).getId());   //sorting wont work, because we are only passing the metadata, we need to sort it manually
    }

    @Test
    void getAuthorById_WhenIdExists_ShouldReturnAuthor() {
        //arrange
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author1));

        //act
        Author result = authorService.getAuthorById(1L);

        //assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("J.K. Rowling", result.getName());
    }
    @Test
    void getAllAuthors_WhenNoAuthorsExist_ShouldReturnEmptyPage() {
        List<Author> emptyListOfAuthors = List.of();
        Page<Author> authorPage = new PageImpl<>(emptyListOfAuthors, pageRequest, 0);
        when(authorRepository.findAll(pageRequest)).thenReturn(authorPage);

        Page<Author> result = authorService.getAllAuthors(0, 10, "ASC", "id");

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(authorRepository,times(1)).findAll(pageRequest);
    }

    @Test
    void getAuthorById_WhenIdDoesNotExist_ShouldThrowException() {
        //arrange
        when(authorRepository.findById(99L)).thenReturn(Optional.empty());

        //act and assert
        assertThrows(AuthorNotFoundException.class, () -> authorService.getAuthorById(99L));
        verify(authorRepository, times(1)).findById(99L);
    }

    @Test
    void saveNewAuthor() {
        Author newAuthor = Author.builder()
                .id(3L)
                .name("Agatha Christie")
                .biography("British writer known for detective novels featuring Hercule Poirot and Miss Marple.")
                .build();
        when(authorRepository.save(newAuthor)).thenReturn(newAuthor);

        Author savedAuthor = authorService.saveNewAuthor(newAuthor);

        assertNotNull(savedAuthor);
        assertEquals(newAuthor, savedAuthor);
        assertEquals(3L, savedAuthor.getId());

    }

    @Test
    void updateAuthor_WhenBookAreChanged_ShouldUpdateSuccessfully() {
        Author updatedAuthor = Author.builder()
                .id(2L)
                .name("George Orwell Updated")
                .biography("Updated Biography")
                .build();
        when(authorRepository.findById(author2.getId())).thenReturn(Optional.of(author2));
        when(authorRepository.save(author2)).thenReturn(author2); //author 2 is an exisiting author, remember that we are setting the data for the existing author (author2) not creating another author

        Author savedAuthor = (authorService.updateAuthor(author2.getId(), updatedAuthor));

        assertNotNull(savedAuthor);
        assertEquals(author2, savedAuthor);
        assertEquals(author2.getName(), savedAuthor.getName());
        assertEquals(author2.getBiography(), savedAuthor.getBiography());
        verify(authorRepository, times(1)).findById(author2.getId());
        verify(authorRepository, times(1)).save(any(Author.class));
    }

    @Test
    void updateAuthor_WithTheSameData_ShouldAvoidUnnecessaryUpdates() {
        Author identicalData = Author.builder()
                .id(2L)
                .name("George Orwell")
                .biography("English novelist, best known for '1984' and 'Animal Farm'.")
                .publishedBooks(List.of(
                        new Book(3L, "1984", "Dystopian", 1949, author2, new ArrayList<>()),
                        new Book(4L, "Animal Farm", "Political Satire", 1945, author2, new ArrayList<>())
                ))
                .build();
        when(authorRepository.findById(author2.getId())).thenReturn(Optional.of(author2));

        Author savedAuthor = authorService.updateAuthor(author2.getId(), identicalData);

        assertEquals(author2, savedAuthor);
        verify(authorRepository,times(1)).findById(author2.getId());
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void deleteAuthorById_WhenIdExists_ShouldDeleteSuccessfully() {
        doNothing().when(authorRepository).deleteById(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        authorService.deleteAuthorById(1L);

        assertThrows(AuthorNotFoundException.class, () -> authorService.getAuthorById(1L));
        verify(authorRepository,times(1)).deleteById(author1.getId());
    }

    @Test
    void getAuthorsByPublishedBookTitle_WhenPublishedBookExist_ShouldReturnAssociatedAuthors() {
        String publishedBookTitle = "1984";
        List<Author> expectedAuthorList = authorList
                .stream()
                .filter(author -> author.getPublishedBooks().stream()
                        .anyMatch(book -> book.getTitle().equalsIgnoreCase(publishedBookTitle)))
                .toList();
        Page<Author> authorPage = new PageImpl<>(expectedAuthorList, pageRequest, expectedAuthorList.size());
        when(authorRepository.findByPublishedBookTitle(publishedBookTitle, pageRequest)).thenReturn((authorPage));

        Page<Author> result = authorService.getAuthorsByPublishedBookTitle(publishedBookTitle, 0,10,"ASC","id");

        assertFalse(result.getContent().isEmpty());
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("George Orwell", result.getContent().get(0).getName());
        List<String> authorsPublishedBooks = result.getContent().get(0).getPublishedBooks()
                .stream()
                .map(Book::getTitle)
                .toList();
        assertTrue(authorsPublishedBooks.contains(publishedBookTitle));
        //assertEquals(result.getContent().get(0).getPublishedBooks().get(0).getTitle(), publishedBookTitle);
        verify(authorRepository, times(1)).findByPublishedBookTitle(publishedBookTitle, pageRequest);
    }

    @Test
    void getAuthorsByPublishedBookTitle_WhenBookTitleNotMatch_ShouldReturnEmptyPage() {
        String nonExisitingBookTitle = "No One's Book";
        List<Author> expectedAuthorList = authorList.stream()
                .filter(author -> author.getPublishedBooks().stream()
                        .anyMatch(book -> book.getTitle().equalsIgnoreCase(nonExisitingBookTitle)))
                .toList();
        Page<Author> authorPage = new PageImpl<>(expectedAuthorList, pageRequest, expectedAuthorList.size());
        when(authorRepository.findByPublishedBookTitle(nonExisitingBookTitle, pageRequest)).thenReturn(authorPage);

        Page<Author> result = authorService.getAuthorsByPublishedBookTitle(nonExisitingBookTitle, 0, 10, "ASC", "id");

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getContent().size());    //size for 1 page
        assertEquals(0, result.getTotalElements()); //all size elements
        verify(authorRepository, times(1)).findByPublishedBookTitle(nonExisitingBookTitle, pageRequest);
    }

    @Test
    void getAuthorsByName_WhenAuthorNameMatch_ShouldReturnPageOfAuthor() {
        String name = "J.K. Rowling";
        List<Author> expectedAuthorList = authorList.stream()
                .filter(author -> author.getName().equalsIgnoreCase(name))
                .toList();
        Page<Author> authorPage = new PageImpl<>(expectedAuthorList, pageRequest, expectedAuthorList.size());
        when(authorRepository.findAuthorByName(name, pageRequest)).thenReturn(authorPage);

        Page<Author> result = authorService.getAuthorsByName(name, 0, 10, "ASC", "id");

        assertFalse(result.getContent().isEmpty());
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(name, result.getContent().get(0).getName());
        verify(authorRepository,times(1)).findAuthorByName(name, pageRequest);
    }

    @Test
    void getAuthorsByName_WhenNameNotFound_ShouldReturnEmptyPage() {
        String name_notExisiting = "Pogi";
        List<Author> expectedAuthorList = authorList.stream()
                .filter(author -> author.getName().equalsIgnoreCase(name_notExisiting))
                .toList();
        Page<Author> authorPage = new PageImpl<>(expectedAuthorList, pageRequest, expectedAuthorList.size());
        when(authorRepository.findAuthorByName(name_notExisiting, pageRequest)).thenReturn(authorPage);

        Page<Author> result = authorService.getAuthorsByName(name_notExisiting, 0, 10, "ASC", "id");

        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(authorRepository,times(1)).findAuthorByName(name_notExisiting, pageRequest);
    }

    @Test
    void getAuthorsByBiographyKeyword_WhenKeywordMatch_ShouldReturnPageOfAuthor() {
        String biographyKeyword = "best known for the Harry Potter series";
        List<Author> expectedAuthorList = authorList.stream()
                .filter(author -> author.getBiography().toLowerCase().contains(biographyKeyword.toLowerCase()))
                .toList();
        Page<Author> authorPage = new PageImpl<>(expectedAuthorList, pageRequest, expectedAuthorList.size());
        when(authorRepository.findByBiographyContainingIgnoreCase(biographyKeyword, pageRequest)).thenReturn(authorPage);

        Page<Author> result = authorService.getAuthorsByBiographyKeyword(biographyKeyword, 0, 10, "ASC", "id");

        assertFalse(result.getContent().isEmpty());
        assertTrue(result.getContent().get(0).getBiography().toLowerCase().contains(biographyKeyword.toLowerCase()));
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(authorRepository,times(1)).findByBiographyContainingIgnoreCase(biographyKeyword, pageRequest);
    }

    @Test
    void getAuthorsByBiographyKeyword_WhenKeywordNotMatch_ShouldReturnEmptyPage() {
        String biographyKeyword = "pogi lang sa gedli";
        List<Author> expectedAuthorList = authorList.stream()
                .filter(author -> author.getBiography().toLowerCase().contains(biographyKeyword.toLowerCase()))
                .toList();
        Page<Author> authorPage = new PageImpl<>(expectedAuthorList, pageRequest, expectedAuthorList.size());
        when(authorRepository.findByBiographyContainingIgnoreCase(biographyKeyword, pageRequest)).thenReturn(authorPage);

        Page<Author> result = authorService.getAuthorsByBiographyKeyword(biographyKeyword, 0, 10, "ASC", "id");

//        assertFalse(authorList.stream()           //not necessary, we should focus on the result
//                .map(author -> author.getBiography().toLowerCase())
//                .anyMatch(bio -> bio.contains(biographyKeyword.toLowerCase())));
        assertTrue(result.getContent().isEmpty());
        assertNotNull(result);      //confirms that metadata still exist, page request information
        assertEquals(0, result.getTotalElements());
        verify(authorRepository,times(1)).findByBiographyContainingIgnoreCase(biographyKeyword, pageRequest);
    }

}