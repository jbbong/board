package com.fastcampus.projectboard.service;

import com.fastcampus.projectboard.domain.Article;
import com.fastcampus.projectboard.domain.type.SearchType;
import com.fastcampus.projectboard.dto.ArticleDto;
import com.fastcampus.projectboard.dto.ArticleWithCommentsDto;
import com.fastcampus.projectboard.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword, Pageable pageable) {
        if(searchKeyword==null || searchKeyword.isBlank()){
            return articleRepository.findAll(pageable).map(ArticleDto::from);
        }
        return switch(searchType){
            case TITLE -> articleRepository.findByTitleContaining(searchKeyword, pageable).map(ArticleDto::from);
            case ID -> articleRepository.findByUserAccount_UserIdContaining(searchKeyword, pageable).map(ArticleDto::from);
            case CONTENT -> articleRepository.findByContentContaining(searchKeyword, pageable).map(ArticleDto::from);
            case HASHTAG -> articleRepository.findByHashtag("#"+searchKeyword, pageable).map(ArticleDto::from);
            case NICKNAME -> articleRepository.findByUserAccount_NicknameContaining(searchKeyword, pageable).map(ArticleDto::from);
        };
    }

    @Transactional(readOnly = true)
    public ArticleWithCommentsDto getArticle(Long articleId) {

        return articleRepository.findById(articleId).map(ArticleWithCommentsDto::from)
                .orElseThrow(()->new EntityNotFoundException("게시글이 없습니다 - articleId: "+articleId));
    }

    public void saveArticle(ArticleDto dto) {
        articleRepository.save(dto.toEntity());
    }

    public void updateArticle(ArticleDto dto) {
        try {
            Article article = articleRepository.getReferenceById(dto.id()); //gotone에서 getreferencebyid로.
            if (dto.title() != null) {
                article.setTitle(dto.title());
            }  //낫널필드
            if (dto.content() != null) {
                article.setContent(dto.content());
            }
            article.setHashtag(dto.hashtag());
            articleRepository.save(article);  //해당 save 메서드 없어도 ok, 메서드 단위로 트잭 묶여있어서 자동으로 저장
        }catch(EntityNotFoundException e){
            log.warn("게시글 업데이트 실패. 게시글 찾을 수 없음-dto:{}",dto);
        }
    }

    public void deleteArticle(long articleId) {
        articleRepository.deleteById(articleId);
    }

}
