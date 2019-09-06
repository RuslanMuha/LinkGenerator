package com.hitecher.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Builder
@ToString
public class URLDto {
    String id;
    String url;
}
