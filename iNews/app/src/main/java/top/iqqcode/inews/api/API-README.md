
## 文档详情

文档地址：https://www.mxnzp.com/doc/detail?id=12

app_id= gspmleqpjxxr1gpa

app_secret= bTZ3L3BreE9PdGlNRTJFMlU4SXV5UT09

### 获取所有新闻类型列表

[link]: https://www.mxnzp.com/api/news/types?app_id=gspmleqpjxxr1gpa&app_secret=bTZ3L3BreE9PdGlNRTJFMlU4SXV5UT09

```
{
    "code": 1,
    "msg": "数据返回成功",
    "data": [
        {
            "typeId": 509,
            "typeName": "财经"
        },
        {
            "typeId": 510,
            "typeName": "科技"
        },
        ...这里只显示了两个...
    ]
}
```


### 根据新闻类型获取新闻列表

[link]: https://www.mxnzp.com/api/news/list?typeId=509&page=1&app_id=gspmleqpjxxr1gpa&app_secret=bTZ3L3BreE9PdGlNRTJFMlU4SXV5UT09

```
{
    "code": 1,
    "msg": "数据返回成功",
    "data": [
        {
            "title": "习近平：巩固机构改革成果 推进治理体系和能力现代化",
            "imgList": null,
            "source": "新华网",
            "newsId": "EJBCPBMK000189FH",
            "digest": "习近平在深化党和国家机构改革总结会议上强调巩固党和国家机构改革成果推进国家治理体系和治理能力现代化李克强栗战书汪洋赵乐际韩正出席王沪宁主持新华社北京7月5日电深",
            "postTime": "2019-07-05 17:45:44",
            "videoList": null
        },
        ...这里只显示一条数据...
    ]
}

```


### 根据新闻id获取新闻详情

[link]:  https://www.mxnzp.com/api/news/details?newsId=EJA5MJQ30001875N&app_id=gspmleqpjxxr1gpa&app_secret=bTZ3L3BreE9PdGlNRTJFMlU4SXV5UT09

```
{
    "code": 1,
    "msg": "数据返回成功",
    "data": {
        "images": [
            {
                "position": "<!--IMG#0-->",
                "imgSrc": "http://cms-bucket.ws.126.net/2019/07/05/86125cd7700c4bc5aa8f8bca16df66be.jpeg",
                "size": "865*772"
            },
            ...这里只显示了一张图片
        ],
        "title": "三峡大坝被传已变形将溃堤 中国航天发卫星图澄清",
        "content": "<p>　　近日，境外一名反华分子在他的社交账号上宣称中国的三峡大坝已经变形，溃堤在即。...",
        "source": "环球时报",
        "ptime": "2019-07-05 06:22:40",
        "docid": "EJA5MJQ30001875N",
        "cover": "http://cms-bucket.ws.126.net/2019/07/05/017d16cc8d5745978c9150012ba69fe4.png"
    }
}

```