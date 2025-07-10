package backend.controller;

import backend.domain.Board;
import backend.domain.User;
import backend.dto.BoardRequestDto;
import backend.dto.BoardResponseDto;
import backend.service.BoardService;
import backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final UserRepository userRepository;

    // 게시글 전체 조회
    @GetMapping
    public List<BoardResponseDto> getAllBoards() {
        return boardService.findAll().stream()
                .map(board -> new BoardResponseDto(
                        board.getId(),
                        board.getTitle(),
                        board.getContent(),
                        board.getCreatedAt(),
                        board.getAuthor().getName()
                ))
                .collect(Collectors.toList());
    }

    // 게시글 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponseDto> getBoard(@PathVariable Long id) {
        return boardService.findById(id)
                .map(board -> ResponseEntity.ok(new BoardResponseDto(
                        board.getId(),
                        board.getTitle(),
                        board.getContent(),
                        board.getCreatedAt(),
                        board.getAuthor().getName()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // 게시글 생성
    @PostMapping
    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto requestDto, Principal principal) {
        // Principal에서 userId 추출 (실제 환경에서는 인증 방식에 따라 다름)
        String userId = principal.getName();
        User author = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));
        Board board = boardService.create(requestDto.getTitle(), requestDto.getContent(), author);
        BoardResponseDto responseDto = new BoardResponseDto(
                board.getId(), board.getTitle(), board.getContent(), board.getCreatedAt(), author.getName()
        );
        return ResponseEntity.ok(responseDto);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<BoardResponseDto> updateBoard(@PathVariable Long id, @RequestBody BoardRequestDto requestDto) {
        return boardService.update(id, requestDto.getTitle(), requestDto.getContent())
                .map(board -> ResponseEntity.ok(new BoardResponseDto(
                        board.getId(),
                        board.getTitle(),
                        board.getContent(),
                        board.getCreatedAt(),
                        board.getAuthor().getName()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        boardService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 