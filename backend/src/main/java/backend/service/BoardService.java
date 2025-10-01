package backend.service;

import backend.domain.Board;
import backend.domain.User;
import backend.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BoardService {
    private final BoardRepository boardRepository;

    // 게시글 전체 조회
    public List<Board> findAll() {
        return boardRepository.findAll();
    }

    // 게시글 단일 조회
    public Optional<Board> findById(Long id) {
        return boardRepository.findById(id);
    }

    // 게시글 생성
    public Board create(String title, String content, User author) {
        Board board = new Board(title, content, author);
        return boardRepository.save(board);
    }

    // 게시글 수정
    public Optional<Board> update(Long id, String title, String content) {
        return boardRepository.findById(id).map(board -> {
            board.setTitle(title);
            board.setContent(content);
            return boardRepository.save(board);
        });
    }

    // 게시글 삭제
    public void delete(Long id) {
        boardRepository.deleteById(id);
    }
} 