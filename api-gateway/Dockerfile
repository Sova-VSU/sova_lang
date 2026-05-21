FROM golang:alpine AS builder
WORKDIR /app
COPY go.mod ./
COPY . .
RUN go mod tidy && go build -o api-gateway .

FROM alpine:latest
WORKDIR /app
COPY --from=builder /app/api-gateway .
EXPOSE 8080
CMD ["./api-gateway"]
