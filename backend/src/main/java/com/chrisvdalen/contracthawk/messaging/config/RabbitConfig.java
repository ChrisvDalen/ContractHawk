package com.chrisvdalen.contracthawk.messaging.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.aopalliance.aop.Advice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
public class RabbitConfig {

    @Bean
    public DirectExchange analysisExchange(MessagingProperties properties) {
        return new DirectExchange(properties.exchange(), true, false);
    }

    @Bean
    public DirectExchange analysisDeadLetterExchange(MessagingProperties properties) {
        return new DirectExchange(properties.deadLetterExchange(), true, false);
    }

    @Bean
    public Queue analysisQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.queue())
                .withArgument("x-dead-letter-exchange", properties.deadLetterExchange())
                .withArgument("x-dead-letter-routing-key", properties.deadLetterRoutingKey())
                .build();
    }

    @Bean
    public Queue analysisDeadLetterQueue(MessagingProperties properties) {
        return QueueBuilder.durable(properties.deadLetterQueue()).build();
    }

    @Bean
    public Binding analysisBinding(Queue analysisQueue, DirectExchange analysisExchange, MessagingProperties properties) {
        return BindingBuilder.bind(analysisQueue).to(analysisExchange).with(properties.routingKey());
    }

    @Bean
    public Binding analysisDeadLetterBinding(Queue analysisDeadLetterQueue,
                                             DirectExchange analysisDeadLetterExchange,
                                             MessagingProperties properties) {
        return BindingBuilder.bind(analysisDeadLetterQueue)
                .to(analysisDeadLetterExchange)
                .with(properties.deadLetterRoutingKey());
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                                MessageConverter converter,
                                                                                MessagingProperties properties) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor(properties));
        return factory;
    }

    private Advice retryInterceptor(MessagingProperties properties) {
        MessagingProperties.Retry retry = properties.retry();

        ExponentialBackOffPolicy backOff = new ExponentialBackOffPolicy();
        backOff.setInitialInterval(retry.initialIntervalMs());
        backOff.setMultiplier(retry.multiplier());
        backOff.setMaxInterval(retry.maxIntervalMs());

        SimpleRetryPolicy policy = new SimpleRetryPolicy(retry.maxAttempts());

        RetryTemplate template = new RetryTemplate();
        template.setBackOffPolicy(backOff);
        template.setRetryPolicy(policy);

        return RetryInterceptorBuilder.stateless()
                .retryOperations(template)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }
}
