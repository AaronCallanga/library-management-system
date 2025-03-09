package com.system.libraryManagementSystem.mapper;

import com.system.libraryManagementSystem.dto.BorrowingRecordDTO;
import com.system.libraryManagementSystem.exception.BookNotFoundException;
import com.system.libraryManagementSystem.exception.MemberNotFoundException;
import com.system.libraryManagementSystem.model.Book;
import com.system.libraryManagementSystem.model.BorrowingRecord;
import com.system.libraryManagementSystem.model.Member;
import com.system.libraryManagementSystem.repository.BookRepository;
import com.system.libraryManagementSystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BorrowingRecordMapper {

    private static MemberRepository memberRepository;
    private static BookRepository bookRepository;

    @Autowired
    public void setMemberRepository(MemberRepository memberRepository) {
        BorrowingRecordMapper.memberRepository = memberRepository;
    }

    @Autowired
    public void setBookRepository(BookRepository bookRepository) {
        BorrowingRecordMapper.bookRepository = bookRepository;
    }

    public static BorrowingRecordDTO toDTO(BorrowingRecord borrowingRecord) {

        BorrowingRecordDTO borrowingRecordDTO = new BorrowingRecordDTO();

        borrowingRecordDTO.setRecordId(borrowingRecord.getId());
        borrowingRecordDTO.setMemberId(borrowingRecord.getMember().getId());
        borrowingRecordDTO.setMemberName(borrowingRecord.getMember().getName());
        borrowingRecordDTO.setBookId(borrowingRecord.getBook().getId());
        borrowingRecordDTO.setBookTitle(borrowingRecord.getBook().getTitle());
        borrowingRecordDTO.setBorrowDate(borrowingRecord.getBorrowDate());
        borrowingRecordDTO.setReturnDate(borrowingRecord.getReturnDate());

        return borrowingRecordDTO;
    }

    public static BorrowingRecord toEntity(BorrowingRecordDTO borrowingRecordDTO) {
        Member member = memberRepository.findById(borrowingRecordDTO.getMemberId())
                .orElseThrow(() -> new MemberNotFoundException("Member not found with the id: " + borrowingRecordDTO.getMemberId()));
        Book book = bookRepository.findById(borrowingRecordDTO.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found with the id: " + borrowingRecordDTO.getBookId()));

        if(!member.getName().equalsIgnoreCase(borrowingRecordDTO.getMemberName())) {
            throw new MemberNotFoundException(borrowingRecordDTO.getMemberName() + " did not match the name of the member with the id: "+ borrowingRecordDTO.getMemberId());
        }   //make this validation anotation
        if(!book.getTitle().equalsIgnoreCase(borrowingRecordDTO.getBookTitle())) {
            throw new BookNotFoundException(borrowingRecordDTO.getBookTitle() + " did not match the title of the book with the id: "+ borrowingRecordDTO.getBookId());
        } //make this validation anotation

        BorrowingRecord borrowingRecord = new BorrowingRecord();

        borrowingRecord.setId(borrowingRecordDTO.getRecordId());
        borrowingRecord.setBook(book);
        borrowingRecord.setMember(member);
        borrowingRecord.setBorrowDate(borrowingRecordDTO.getBorrowDate());
        borrowingRecord.setReturnDate(borrowingRecordDTO.getReturnDate());

        return borrowingRecord;
    }
}


//kung sakaling DTO toEntity() - save record
// maybe you need to have repository that can find member by name
// and repository than can find book by title
//  Book book = repo.findByTitle()
//  Member member = repo.findByName()
//  BorrowingRecord record = new BorrowingRecord()
// record.setMember(member)
// record.setBook(book)
// record.setBorrowDatem record.setReturnDate
// borrowingRecordRepository.save(record)